name := "housekeeper"

organization := "com.gu"

description:= "Housekeeping for Ophan"

version := "1.0"

scalaVersion := "2.13.5"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "com.amazonaws" % "aws-lambda-java-events" % "2.2.7",
  "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.28", //  log4j-over-slf4j provides `org.apache.log4j.MDC`, which is dynamically loaded by the Lambda runtime
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.642",
  "com.amazonaws" % "aws-java-sdk-sns" % "1.11.642",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.0.pr3", // So many Snyk warnings
  "com.typesafe.play" %% "play-json" % "2.7.4",
  "org.scanamo" %% "scanamo" % "1.0.0-M11",
  "org.scanamo" %% "scanamo-testkit" % "1.0.0-M11" % "test",

  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

enablePlugins(RiffRaffArtifact, BuildInfoPlugin)

assemblyJarName := s"${name.value}.jar"
riffRaffPackageType := assembly.value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), s"${name.value}-cfn/cfn.yaml")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}


startDynamoDBLocal := startDynamoDBLocal.dependsOn(compile in Test).value

dynamoDBLocalPort := 8042

inConfig(Test)(Seq(
  test := (test in Test).dependsOn(startDynamoDBLocal).value,
  testOnly := (testOnly in Test).dependsOn(startDynamoDBLocal).evaluated,
  testQuick := (testQuick in Test).dependsOn(startDynamoDBLocal).evaluated,
  testOptions += dynamoDBLocalTestCleanup.value
))


buildInfoPackage := "housekeeper"
buildInfoKeys := Seq[BuildInfoKey](
  BuildInfoKey.constant("buildNumber", Option(System.getenv("BUILD_NUMBER")) getOrElse "DEV"),
  // so this next one is constant to avoid it always recompiling on dev machines.
  // we only really care about build time on teamcity, when a constant based on when
  // it was loaded is just fine
  BuildInfoKey.constant("buildTime", System.currentTimeMillis),
  BuildInfoKey.constant("gitCommitId", Option(System.getenv("BUILD_VCS_NUMBER")) getOrElse "DEV")
)