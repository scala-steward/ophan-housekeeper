organization in ThisBuild := "com.gu"

val emailCleanup =
  (project in file("./housekeeper"))

val root = (project in file("."))
  .aggregate(emailCleanup)
