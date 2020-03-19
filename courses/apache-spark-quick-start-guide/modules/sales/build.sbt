val sparkVersion = "3.0.0-preview2"

lazy val root = Project("sales", file("."))
  .settings(
    name := "sales",
    organization := "com.backwards",
    sbtVersion := "1.3.8",
    scalaVersion := "2.12.10",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      "jitpack" at "https://jitpack.io",
      "Confluent Platform Maven" at "http://packages.confluent.io/maven/"
    ),
    autoStartServer := false,
    watchTriggeredMessage := Watch.clearScreenOnTrigger,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
    fork := true,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion
    ),
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      artifact.name + "_" + sv.binary + "-" + sparkVersion + "_" + module.revision + "." + artifact.extension
    }
  )