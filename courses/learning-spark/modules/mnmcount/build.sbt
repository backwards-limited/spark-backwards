lazy val root = Project("mnmcount", file("."))
  .settings(
    name := "mnmcount",
    organization := "com.backwards",
    sbtVersion := "1.6.1",
    scalaVersion := "2.13.8",
    autoStartServer := false,
    watchTriggeredMessage := Watch.clearScreenOnTrigger,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    Compile /doc / scalacOptions ++= Seq("-groups", "-implicits"),
    fork := true,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.2.1",
      "org.apache.spark" %% "spark-sql" % "3.2.1"
    )
  )