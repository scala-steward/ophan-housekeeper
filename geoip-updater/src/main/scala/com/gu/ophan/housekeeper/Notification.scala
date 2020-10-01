package com.gu.ophan.housekeeper

import BounceNotification._
import play.api.libs.json.{Json, Reads}

case class BounceNotification(bounce: Bounce, mail: Mail)

object BounceNotification {

  case class BouncedRecipient(emailAddress: String)
  case class Bounce(bounceType: String, bounceSubType: String, bouncedRecipients: Seq[BouncedRecipient]) {
    val bouncedEmailAddresses = bouncedRecipients.map(_.emailAddress).toSet

    val isPermanent = bounceType.equalsIgnoreCase("permanent")

    val isOnSuppressionList = bounceSubType.equalsIgnoreCase("suppressed")
  }

  case class CommonHeaders(subject: String)
  case class Mail(source: String, messageId: String, destination: Seq[String], commonHeaders: CommonHeaders)


  implicit val readsBouncedRecipient: Reads[BouncedRecipient] = Json.reads[BouncedRecipient]
  implicit val readsBounce: Reads[Bounce] = Json.reads[Bounce]
  implicit val readsCommonHeaders: Reads[CommonHeaders] = Json.reads[CommonHeaders]
  implicit val readsMail: Reads[Mail] = Json.reads[Mail]
  implicit val readsBounceNotification: Reads[BounceNotification] = Json.reads[BounceNotification]
}