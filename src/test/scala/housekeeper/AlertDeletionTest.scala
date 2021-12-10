package housekeeper

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._
import housekeeper.Dynamo.OphanAlert
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scanamo.{LocalDynamoDB, Scanamo}

import scala.util.Random

class AlertDeletionTest extends AnyFlatSpec with Matchers {

  lazy val client = LocalDynamoDB.client()

  "AlertDeletion" should "delete all the alerts for the specified email address" in {
    val scanamo = Scanamo(client)

    val tableName = "TEST-ophan-alerts-"+Random.alphanumeric.take(5).mkString
    LocalDynamoDB.createTable(client)(tableName)("email" -> S, "alertName" -> S)

    val alertDeletion = new AlertDeletion(scanamo, tableName)
    val table = alertDeletion.table

    def allAlerts(): Set[OphanAlert] = { scanamo.exec(table.scan()).flatMap(_.toOption).toSet }

    val bouncingEmailAddress = "user.left@g.com"
    val alertsForBadEmail = (1 to 3).toSet[Int].map(n => OphanAlert(bouncingEmailAddress, s"Dead Alert $n"))
    val alertsForRemainingUsers =
      ('a' to 'c').toSet[Char].map(n => OphanAlert(s"user.$n@g.com", s"Alert 1"))

    scanamo.exec(table.putAll(alertsForRemainingUsers ++ alertsForBadEmail))

    allAlerts() shouldBe (alertsForRemainingUsers ++ alertsForBadEmail)

    alertDeletion.deleteAllAlertsForEmailAddress(bouncingEmailAddress)

    allAlerts() shouldBe (alertsForRemainingUsers)
  }

}
