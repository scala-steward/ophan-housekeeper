organization in ThisBuild := "com.gu"

val emailCleanup =
  (project in file("./housekeeper"))
  .enablePlugins(RiffRaffArtifact)

val geoIpUpdater =
  (project in file("./geoip-updater"))
  .enablePlugins(RiffRaffArtifact)

val root = (project in file("."))
  .aggregate(emailCleanup, geoIpUpdater)
