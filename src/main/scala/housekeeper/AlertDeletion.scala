package housekeeper

import cats.implicits._
import com.amazonaws.regions.Regions.EU_WEST_1
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import housekeeper.Dynamo.OphanAlert
import org.scanamo._
import org.scanamo.syntax._
import org.scanamo.auto._

object Dynamo extends Logging {

  val scanamo = Scanamo(AmazonDynamoDBAsyncClient.asyncBuilder().withRegion(EU_WEST_1).build())

  case class OphanAlert(
                         email: String,
                         alertName: String
                       ) {
    val primaryKey = "email" -> email and "alertName" -> alertName
  }
}

class AlertDeletion(scanamo: Scanamo, tableName: String) extends Logging {

  val table = Table[OphanAlert](tableName)

  def deleteAllAlertsForEmailAddress(email: String): Unit = {
    val baseContext = Map("alertsToDelete.emailAddress" -> email)
    logger.info(baseContext, s"About to search for alerts to delete for '$email'")
    scanamo.exec(for {
      results <- table.query("email" -> email)
      alertsForEmail = results.flatMap(_.toOption)
      _ = logger.info(baseContext ++ Map(
        "alertsToDelete.found.count" -> alertsForEmail.size,
      ), s"About to delete ${alertsForEmail.size} alerts for '$email'")
      deletionResults <- alertsForEmail.traverse(alert => table.delete(alert.primaryKey))
    } yield {
      logger.info(baseContext, s"...successfully deleted ${alertsForEmail.size} alerts for '$email'}")
    })
  }

}
