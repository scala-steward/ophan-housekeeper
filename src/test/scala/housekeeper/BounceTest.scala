package housekeeper

import org.scalatest.{ FlatSpec, Matchers }
import play.api.libs.json.{ JsValue, Json }

class BounceTest extends FlatSpec with Matchers {

  /*
    This is what an SNS event looks like when sent from AWS.
    I'm writing it here mainly for documentation purposes, so we know what an
    `com.amazonaws.services.lambda.runtime.events.SNSEvent` looks like.
    When testing out Lambda from the AWS console this is the kind of stuff we have to feed in.

    The real message (the one we would receive in the email) is in the "Message" field.
   */
  val SNSMockEvent: String =
    """
      |{
      |    "Records": [
      |        {
      |            "EventSource": "aws:sns",
      |            "EventVersion": "1.0",
      |            "EventSubscriptionArn": "arn:aws:sns:eu-west-1:1234567890:ses-email-bounce-notifications-for-housekeeper:924d84dc-6fed-41fc-9e5d-6f4b5a9899d5",
      |            "Sns": {
      |                "Type": "Notification",
      |                "MessageId": "1d1614ba-9e59-5d83-bc74-466c34316619",
      |                "TopicArn": "arn:aws:sns:eu-west-1:1234567890:ses-email-bounce-notifications-for-housekeeper",
      |                "Subject": null,
      |                "Message": "{\"notificationType\":\"Bounce\",\"bounce\":{\"bounceType\":\"Transient\",\"bounceSubType\":\"General\",\"bouncedRecipients\":[{\"emailAddress\":\"some.guy@guardian.co.uk\"}],\"timestamp\":\"2018-01-15T13:10:00.000Z\",\"feedbackId\":\"01000160f9f0de87-948f1f38-e666-41bb-b78a-f8aed2c4d09a-000000\"},\"mail\":{\"timestamp\":\"2018-01-15T13:10:00.684Z\",\"source\":\"Ophan Alerts<somethingliketrigr@ophan.co.uk>\",\"sourceArn\":\"arn:aws:ses:eu-west-1:1234567890:identity/ophan.co.uk\",\"sourceIp\":\"52.50.14.1\",\"sendingAccountId\":\"1234567890\",\"messageId\":\"01000160f9f0d343-0978f934-a519-4e6e-bb98-3b53a1f106fe-000000\",\"destination\":[\"some.one@guardian.co.uk\"]}}",
      |                "Timestamp": "2018-01-15T13:10:00.742Z",
      |                "SignatureVersion": "1",
      |                "Signature": "SzDosyVrZ62IzyOmueRnO+1lxYT79/EBqH5OR+nSb88YVnozwsiOIyTSp+PyxsT+Ve/XzmmdI8iRKwaqWYrCoeLOIOyIyjofjBfZ5fKCu7BC76eWlKTfnUzHBRhzAh4hbaea5tz9kdHOpFw76kJ9aPw1+1bhrfCjh3GVkl2w2894Yg2sio/v96bfWUnBF17t5Gl5CAbCZ1MjF5kzeL62cRkbY7D+5+Q+/T6gPG3niIIj0f89w8DX2crOMoOcmnQB7fyTfaydi5F1b2QsI4woIKQD41zTisyH1ddGs8meKJXVrA+c3jz/VGQuTzEycB6S/fb0W9cdr2FJGB4vzF2RvA==",
      |                "SigningCertUrl": "https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-433026a4050d206028891664da859041.pem",
      |                "UnsubscribeUrl": "https://sns.eu-west-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-1:1234567890:ses-email-bounce-notifications-for-housekeeper:924d84dc-6fed-41fc-9e5d-6f4b5a9899d5",
      |                "MessageAttributes": {}
      |            }
      |        }
      |    ]
      |}
    """.stripMargin

  it should "Not blow up at runtime if garbage json is fed in" in {

    Lambda.go("boom")

  }

  // This is an example of the Message (same as the one above, but prettified)

  val rawMessageJson =
    """
      |{
      |   "notificationType":"Bounce",
      |   "bounce":{
      |      "bounceType":"Permanent",
      |      "bounceSubType":"General",
      |      "bouncedRecipients":[
      |         {
      |            "emailAddress":"xxx@guardian.co.uk"
      |         }
      |      ],
      |      "timestamp":"2018-01-12T12:05:43.000Z",
      |      "feedbackId":"01000160ea42f346-599a6ae8-689c-4178-876d-2eb95026037a-000000"
      |   },
      |   "mail":{
      |      "timestamp":"2018-01-12T12:05:44.447Z",
      |      "source":"Ophan Alerts<somethingliketrigr@ophan.co.uk>",
      |      "sourceArn":"arn:aws:ses:eu-west-1:1234567890:identity/ophan.co.uk",
      |      "sourceIp":"52.50.14.1",
      |      "sendingAccountId":"1234567890",
      |      "messageId":"01000160ea42e7d0-7f50ec93-d337-42b1-89b0-e4f78eee9054-000000",
      |      "destination":[
      |         "xxx@guardian.co.uk"
      |      ]
      |   }
      |}
    """.stripMargin

  it should "Tell if a bounce email is transient bounce (OOO) or permanent (email deleted)" in {

    Lambda.getBounceType(Json.parse(rawMessageJson)) should be(Right(PermanentBounce(Json.parse(rawMessageJson))))
  }

  it should "Remove speechmarks from the email" in {

    val json = Json.parse(rawMessageJson)
    val emails = (json \ "mail" \ "destination").as[List[JsValue]]

    emails foreach { e =>
      Lambda.removeSpeechmarks(e) should be("xxx@guardian.co.uk")
    }

  }

}
