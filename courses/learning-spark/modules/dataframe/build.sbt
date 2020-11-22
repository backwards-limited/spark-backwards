lazy val root = Project("dataframe", file("."))
  .settings(
    name := "dataframe",
    organization := "com.backwards",
    sbtVersion := "1.4.2",
    scalaVersion := "2.12.10",
    autoStartServer := false,
    watchTriggeredMessage := Watch.clearScreenOnTrigger,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
    fork := true,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.0.1",
      "org.apache.spark" %% "spark-sql" % "3.0.1"
    )
  )