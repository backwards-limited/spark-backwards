import BuildProperties._
import sbt._

lazy val root =
  project("spark-backwards", file("."))
    .settings(description := "Backwards Spark module aggregation - Spark functionality includes example usage in various courses")
    .aggregate(spark)
    .aggregate(demoGitActions)
    //.aggregate(bigDataAnalysisWithScalaAndSpark)
    //.aggregate(sparkByExamples)
    //.aggregate(learningSpark, mnmcount, dataframe)
    //.aggregate(masterSpark)
    //.aggregate(bigDataWithSparkEmr)
    //.aggregate(bigDataHadoopSpark)
    //.aggregate(sparkInAction)
    //.dependsOn(spark, learningSpark, masterSpark, bigDataWithSparkEmr, bigDataHadoopSpark, sparkInAction)

lazy val spark =
  project("spark", file("spark"))
    .settings(
      Test / publishArtifact := true,
      IntegrationTest / publishArtifact := true,
      addArtifact(IntegrationTest / packageBin / artifact, IntegrationTest / packageBin).settings
    )

lazy val demoGitActions =
  project("demo-git-actions", file("demo-git-actions"))
    .dependsOn(spark)

lazy val bigDataAnalysisWithScalaAndSpark =
  project("big-data-analysis-with-scala-and-spark", file("courses/big-data-analysis-with-scala-and-spark"))

lazy val sparkByExamples =
  project("spark-by-examples", file("courses/spark-by-examples"))

lazy val learningSpark =
  project("learning-spark", file("courses/learning-spark"))

lazy val mnmcount =
  project("mnmcount", file("courses/learning-spark/modules/mnmcount"))

lazy val dataframe =
  project("dataframe", file("courses/learning-spark/modules/dataframe"))

lazy val masterSpark =
  project("master-spark", file("courses/master-spark"))

lazy val bigDataWithSparkEmr =
  project("big-data-with-spark-emr", file("courses/big-data-with-spark-emr"))

lazy val bigDataHadoopSpark =
  project("big-data-hadoop-spark", file("courses/big-data-hadoop-spark"))

lazy val sparkAndHadoopCourse =
  project("spark-and-hadoop", file("courses/spark-and-hadoop"))

lazy val sparkInAction =
  project("spark-in-action", file("courses/spark-in-action")).dependsOn(spark)

def project(id: String, base: File): Project =
  Project(id, base)
    .enablePlugins(JavaAppPackaging, DockerComposePlugin)
    .settings(
      resolvers ++= Seq(
        Resolver sonatypeRepo "releases",
        Resolver sonatypeRepo "snapshots",
        Resolver.JCenterRepository
      ),
      scalaVersion := "2.13.8",
      sbtVersion := BuildProperties("sbt.version"),
      organization := "com.backwards", // TODO - tech.backwards
      name := id,
      autoStartServer := false,
      watchTriggeredMessage := Watch.clearScreenOnTrigger,
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      libraryDependencies ++= Dependencies(),
      dependencyOverrides ++= Dependencies.overrides,
      fork := true,
      IntegrationTest / javaOptions ++= environment.map { case (key, value) => s"-D$key=$value" }.toSeq,
      IntegrationTest / testFrameworks := Seq(TestFrameworks.ScalaTest),
      // Compile / doc / scalacOptions ++= Seq("-groups", "-implicits", "-Ylog-classpath"),
      scalacOptions ++= Seq(
        "-encoding", "utf8",
        "-deprecation",
        "-unchecked",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-Ymacro-annotations",
        "-Yrangepos",
        "-P:kind-projector:underscore-placeholders" // Can use _ instead of * when defining anonymous type lambdas
        //"-Xfatal-warnings"
        // "-Ywarn-value-discard"
      ),
      Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated,
      Compile / runMain := Defaults.runMainTask(Compile / fullClasspath, Compile / run / runner).evaluated
    )
    .configs(IntegrationTest extend Test)
    .settings(inConfig(IntegrationTest extend Test)(Defaults.testSettings): _*)
    .settings(Defaults.itSettings: _*)
    .settings(
      // To use 'dockerComposeTest' to run tests in the 'IntegrationTest' scope instead of the default 'Test' scope:
      // 1) Package the tests that exist in the IntegrationTest scope
      testCasesPackageTask := (IntegrationTest / packageBin).value,
      // 2) Specify the path to the IntegrationTest jar produced in Step 1
      testCasesJar := (IntegrationTest / packageBin / artifactPath).value.getAbsolutePath,
      // 3) Include any IntegrationTest scoped resources on the classpath if they are used in the tests
      testDependenciesClasspath := {
        val fullClasspathCompile = (Compile / fullClasspath).value
        val classpathTestManaged = (IntegrationTest / managedClasspath).value
        val classpathTestUnmanaged = (IntegrationTest / unmanagedClasspath).value
        val testResources = (IntegrationTest / resources).value
        (fullClasspathCompile.files ++ classpathTestManaged.files ++ classpathTestUnmanaged.files ++ testResources).map(_.getAbsoluteFile).mkString(java.io.File.pathSeparator)
      }
    )
    .settings(
      assembly / assemblyJarName := s"$id.jar",
      assembly / test := {},
      assembly / mainClass := Some(System.getProperty("mainClass")),
      assembly / assemblyMergeStrategy := {
        case x if Assembly.isConfigFile(x) =>
          MergeStrategy.concat

        case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
          MergeStrategy.rename

        case PathList("META-INF", xs @ _*) =>
          xs map {_.toLowerCase} match {
            case "manifest.mf" :: Nil | "index.list" :: Nil | "dependencies" :: Nil =>
              MergeStrategy.discard

            case ps @ x :: xs if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
              MergeStrategy.discard

            case "plexus" :: _ =>
              MergeStrategy.discard

            case "services" :: _ =>
              MergeStrategy.filterDistinctLines

            case "spring.schemas" :: Nil | "spring.handlers" :: Nil =>
              MergeStrategy.filterDistinctLines

            case _ => MergeStrategy.first
          }

        case _ =>
          MergeStrategy.first
      }
    )