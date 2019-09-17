package housekeeper

import org.scalatest.{FlatSpec, Inside, Matchers}
import play.api.libs.json.Json

import scala.io.Source.fromResource

class BounceTest extends FlatSpec with Matchers with Inside {

  it should "recognise a *permanent* bounce notification for an Ophan alert sent to a dead email address" in {
    val notification = bounceNotificationFrom("notificationMessages/permanentBounce.ophanAlert.json")

    notification.bounce.isPermanent shouldBe true
    notification.bounce.bouncedEmailAddresses shouldBe Set("firstName1.lastName1@guardian.co.uk")

    notification.mail.source shouldBe "Ophan Alerts<somethingLikeTrigr@ophan.co.uk>"
    notification.mail.commonHeaders.subject shouldBe "Tech story needs help! alert for 'Building a video game industry from scratch: Chips with Everything podcast'"
  }

  it should "recognise a *transient* bounce notification - the 'out-of-office' kind we don't care about" in {
    val notification = bounceNotificationFrom("notificationMessages/transientBounce.ophanAlert.json")

    notification.bounce.isPermanent shouldBe false
  }

  it should "recognise a permanent bounce notification - even it's from a non-Ophan project - so we can monitor and alert relevant teams" in {
    val notification = bounceNotificationFrom("notificationMessages/permanentBounce.airflow.json")

    notification.bounce.isPermanent shouldBe true
    notification.mail.source shouldBe "somethingLikeAirflow@ophan.co.uk"
  }

  def bounceNotificationFrom(resource: String) = Json.parse(fromResource(resource).mkString).as[BounceNotification]
}
