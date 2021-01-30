import BuildProperties._
import sbt._

lazy val root =
  project("spark-backwards", file("."))
    .settings(description := "Backwards Spark module aggregation - Spark functionality includes example usage in various courses")
    .aggregate(spark)
    .aggregate(demoGitActions)
    .aggregate(bigDataAnalysisWithScalaAndSpark)
    .aggregate(sparkByExamples)
    .aggregate(learningSpark, mnmcount, dataframe)
    .aggregate(masterSpark)
    .aggregate(bigDataWithSparkEmr)
    .aggregate(bigDataHadoopSpark)
    .dependsOn(spark, learningSpark, masterSpark, bigDataWithSparkEmr, bigDataHadoopSpark) // TODO - Should I get rid of this?

lazy val spark =
  project("spark", file("spark"))
    .settings(
      publishArtifact in Test := true,
      publishArtifact in IntegrationTest := true,
      addArtifact(artifact in (IntegrationTest, packageBin), packageBin in IntegrationTest).settings
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

// TODO - Put back
lazy val sparkAndHadoopCourse =
  project("spark-and-hadoop", file("courses/spark-and-hadoop"))

def project(id: String, base: File): Project =
  Project(id, base)
    .enablePlugins(JavaAppPackaging, DockerComposePlugin)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(testFrameworks in IntegrationTest := Seq(TestFrameworks.ScalaTest))
    .settings(
      resolvers ++= Seq(
        "jitpack" at "https://jitpack.io"
      ),
      scalaVersion := BuildProperties("scala.version"),
      sbtVersion := BuildProperties("sbt.version"),
      organization := "com.backwards",
      name := id,
      autoStartServer := false,
      watchTriggeredMessage := Watch.clearScreenOnTrigger,
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      libraryDependencies ++= Dependencies(),
      dependencyOverrides ++= Dependencies.overrides,
      fork := true,
      javaOptions in IntegrationTest ++= environment.map { case (key, value) => s"-D$key=$value" }.toSeq,
      scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
      run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated,
      runMain in Compile := Defaults.runMainTask(fullClasspath in Compile, runner in (Compile, run)).evaluated,
      assemblyJarName in assembly := s"$id.jar",
      test in assembly := {},
      mainClass in assembly := Some(System.getProperty("mainClass")),
      assemblyMergeStrategy in assembly := {
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
    .settings(
      // To use 'dockerComposeTest' to run tests in the 'IntegrationTest' scope instead of the default 'Test' scope:
      // 1) Package the tests that exist in the IntegrationTest scope
      testCasesPackageTask := (sbt.Keys.packageBin in IntegrationTest).value,
      // 2) Specify the path to the IntegrationTest jar produced in Step 1
      testCasesJar := artifactPath.in(IntegrationTest, packageBin).value.getAbsolutePath,
      // 3) Include any IntegrationTest scoped resources on the classpath if they are used in the tests
      testDependenciesClasspath := {
        val fullClasspathCompile = (fullClasspath in Compile).value
        val classpathTestManaged = (managedClasspath in IntegrationTest).value
        val classpathTestUnmanaged = (unmanagedClasspath in IntegrationTest).value
        val testResources = (resources in IntegrationTest).value
        (fullClasspathCompile.files ++ classpathTestManaged.files ++ classpathTestUnmanaged.files ++ testResources).map(_.getAbsoluteFile).mkString(java.io.File.pathSeparator)
      }
    )