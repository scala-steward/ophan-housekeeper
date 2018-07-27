package housekeeper

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient

object Dynamo {

  val dyClient = AmazonDynamoDBAsyncClient.asyncBuilder().withRegion(Regions.EU_WEST_1).build()

  import com.gu.scanamo._
  import com.gu.scanamo.syntax._

  case class Alert(
    email: String,
    alertName: String,
    query: Option[String],
    threshold: Option[String],
    filters: Option[String],
    viewsPerMinute: Option[Int])

  val table = Table[Alert]("ophan-alerts")

  def deleteAlert(email: String): Unit = {

    // we need to get all the alertNames for a certain email address:
    val alertNamesForEmail = Scanamo.exec(dyClient)(table.query('email -> email))
      .flatMap(_.toOption)
      .map(_.alertName)

    println("The following email alerts will be deleted:")
    alertNamesForEmail foreach println
    println("\n")

    alertNamesForEmail foreach { aName =>
      Scanamo.exec(dyClient)(table.delete('email -> email and 'alertName -> aName))
    }

    println("Done! :)")
  }

}
