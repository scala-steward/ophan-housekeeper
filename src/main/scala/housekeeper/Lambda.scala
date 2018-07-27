package housekeeper

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import play.api.libs.json.{ JsString, JsValue, Json, JsonValidationError }

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

sealed trait BounceType
case class PermanentBounce(json: JsValue) extends BounceType
case class TransientBounce(json: JsValue) extends BounceType

object Lambda {

  def removeSpeechmarks(email: JsValue): String = email.toString.replaceAll("\"", "")

  def getBounceType(json: JsValue): Either[JsonValidationError, BounceType] = {

    (json \ "bounce" \ "bounceType").toEither match {
      case Right(v) => Right(if (v == JsString("Permanent")) PermanentBounce(json) else TransientBounce(json))
      case Left(err) => Left(err)
    }
  }

  def handleBounce(bounce: BounceType): Unit = bounce match {
    case PermanentBounce(json) => {
      println(s"Got PERMANENT bounce:\n $json\n; going to delete the entry in dynamo")

      val expiredEmail = (json \ "mail" \ "destination").as[List[JsValue]]
      expiredEmail foreach { email =>
        val cleanEmail = removeSpeechmarks(email)
        println(s"Searching entries for email $cleanEmail")
        Dynamo.deleteAlert(cleanEmail)
      }
    }
    case TransientBounce(_) => println(s"Got TRANSIENT bounce; nothing to do yet")
  }

  /*
   * Logic handler
   */
  def go(message: String): Unit = {

    Try(Json.parse(message)) match {
      case Success(validJson) => {
        getBounceType(validJson) match {
          case Left(err) => println(s"$err")
          case Right(bounce) => handleBounce(bounce)
        }
      }
      case Failure(fail) => println(s"$fail")
    }
  }

  /*
   * Lambda's entry point
   */
  def handler(lambdaInput: SNSEvent, context: Context): Unit = {
    lambdaInput.getRecords.asScala.map(_.getSNS.getMessage).toList.headOption foreach go
  }
}

