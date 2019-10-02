package housekeeper

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers.appendEntries
import scala.collection.JavaConverters._

trait Logging {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  implicit def mapToContext(c: Map[String, _]): LogstashMarker = appendEntries(c.asJava)

}