import BuildProperties._
import sbt._

lazy val root = project("spark-backwards", file("."))
  .settings(description := "Backwards Spark module aggregation - Spark functionality includes example usage in various courses")
  .aggregate(apacheSparkQuickStartGuide)
  .aggregate(learningSpark)
  .aggregate(mnmcount)
  // .aggregate(sparkAndHadoopCourse)

lazy val apacheSparkQuickStartGuide = project("apache-spark-quick-start-guide", file("courses/apache-spark-quick-start-guide"))
  .settings(description := "Apache Spark Quick Start Guide Book")

lazy val learningSpark = project("learning-spark", file("courses/learning-spark"))
  .settings(description := "Learning Spark Book")

lazy val mnmcount = project("mnmcount", file("courses/learning-spark/modules/mnmcount"))
  .settings(description := "M&M Count")

lazy val sparkAndHadoopCourse = project("spark-and-hadoop", file("courses/spark-and-hadoop"))
  .settings(description := "Spark and Hadoop Course")

def project(id: String, base: File): Project =
  Project(id, base)
    .enablePlugins(JavaAppPackaging, DockerComposePlugin)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(testFrameworks in IntegrationTest := Seq(TestFrameworks.ScalaTest))
    .settings(
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.bintrayRepo("cakesolutions", "maven"),
        "jitpack" at "https://jitpack.io",
        "Confluent Platform Maven" at "http://packages.confluent.io/maven/"
      ),
      scalaVersion := BuildProperties("scala.version"),
      sbtVersion := BuildProperties("sbt.version"),
      organization := "com.backwards",
      name := id,
      autoStartServer := false,
      watchTriggeredMessage := Watch.clearScreenOnTrigger,
      addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      libraryDependencies ++= Dependencies(),
      dependencyOverrides ++= Dependencies.overrides,
      fork := true,
      javaOptions in IntegrationTest ++= environment.map { case (key, value) => s"-D$key=$value" }.toSeq,
      scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
      assemblyJarName in assembly := s"$id.jar",
      test in assembly := {},
      assemblyMergeStrategy in assembly := {
        case PathList("javax", xs @ _*) => MergeStrategy.first
        case PathList("org", xs @ _*) => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first
        case "application.conf" => MergeStrategy.concat
        case "codegen/config.fmpp" => MergeStrategy.concat
        case "git.properties" => MergeStrategy.concat
        case "parquet.thrift" => MergeStrategy.concat
        case "plugin.xml" => MergeStrategy.concat
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
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