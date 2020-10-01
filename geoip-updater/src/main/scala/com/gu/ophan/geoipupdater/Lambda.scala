package com.gu.ophan.housekeeper

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.model.PublishRequest
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.jdk.CollectionConverters._

object Lambda extends Logging {

  /*
   * Lambda's entry point
   */
  def handler(lambdaInput: SNSEvent, context: Context): Unit = {
    logger.info("Hello world!")
  }

}

