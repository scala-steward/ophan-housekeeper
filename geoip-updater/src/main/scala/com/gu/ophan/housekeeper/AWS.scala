package com.gu.ophan.housekeeper

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider}
import com.amazonaws.regions.Regions.EU_WEST_1
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.sns.AmazonSNSAsyncClient

object AWS {
  val credentials = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider,
    new ProfileCredentialsProvider("ophan")
  )

  val SNS = AmazonSNSAsyncClient.asyncBuilder().withCredentials(credentials).withRegion(EU_WEST_1).build()

  val Dynamo = AmazonDynamoDBAsyncClient.asyncBuilder().withCredentials(credentials).withRegion(EU_WEST_1).build()
}
