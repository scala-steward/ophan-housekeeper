package housekeeper

import ch.qos.logback.classic.spi.ILoggingEvent
import com.amazonaws.services.lambda.runtime.LambdaRuntimeInternal
import net.logstash.logback.layout.LogstashLayout
import play.api.libs.json.Json

class LogstashLayoutWithBuildInfo extends LogstashLayout {
  LambdaRuntimeInternal.setUseLog4jAppender(true)

  def getContextTags: Map[String, String] = Map(
    "buildNumber" -> BuildInfo.buildNumber,
    "gitCommitId" -> BuildInfo.gitCommitId
  )

  def makeCustomFields(customFields: Map[String, String]): String = Json.stringify(Json.toJsObject(customFields))

  setCustomFields(makeCustomFields(getContextTags))

  override def doLayout(event: ILoggingEvent): String = super.doLayout(event)+"\n"
}
