package housekeeper

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.collection.JavaConverters._

object Lambda extends Logging {

  private val alertDeletion = new AlertDeletion(Dynamo.scanamo, "ophan-alerts")

  def handleBounce(bounceNotification: BounceNotification): Unit = {
    val bounce = bounceNotification.bounce

    val bouncedAddresses = bounce.bouncedEmailAddresses.toSeq.sorted

    logger.info(Map(
      "bounce.type" -> bounce.bounceType,
      "bounce.subtype" -> bounce.bounceSubType,
      "bounce.permanent" -> bounce.isPermanent,
      "bounce.mail.source" -> bounceNotification.mail.source,
      "bounce.bouncedEmailAddresses" -> bouncedAddresses.asJava
    ), s"${bounceNotification.mail.source} sent a ${bounce.bounceType} bounced message to ${bouncedAddresses.mkString(",")}")

    if (bounce.isPermanent) {
      bouncedAddresses.foreach(alertDeletion.deleteAllAlertsForEmailAddress)
    }
  }

  /*
   * Logic handler
   */
  def go(message: String): Unit = {
    logger.info(Map(
      "notification.message" -> message
    ), s"Running with notification message")

    Json.parse(message).validate[BounceNotification] match {
      case JsSuccess(bounceNotification, _) => {
        handleBounce(bounceNotification)
      }
      case fail: JsError  =>
        logger.error(Map("notification.message" -> message),"Couldn't parse message as BounceNotification",fail)
    }
  }

  /*
   * Lambda's entry point
   */
  def handler(lambdaInput: SNSEvent, context: Context): Unit = {
    lambdaInput.getRecords.asScala.map(_.getSNS.getMessage).toList.headOption foreach go
  }

}

