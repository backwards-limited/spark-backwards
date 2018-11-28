import BuildProperties._
import Dependencies._
import sbt._

lazy val root = project("spark-backwards", file("."))
  .settings(description := "Backwards Spark module aggregation - Spark functionality includes example usage in various courses")
  .aggregate(spark, sparkCourse)

lazy val spark = project("spark")
  .settings(description := "Backwards Spark functionality includes example usage in various courses")
  .settings(javaOptions in Test ++= Seq("-Dconfig.resource=application.test.conf"))

lazy val sparkCourse = project("spark-course")
  .settings(description := "Spark Course")
  .dependsOn(spark % "compile->compile;test->test;it->it")

def project(id: String): Project = project(id, file(id))

def project(id: String, base: File): Project =
  Project(id, base)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
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
      addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8"),
      libraryDependencies ++= dependencies,
      fork in Test := true,
      fork in IntegrationTest := true,
      javaOptions in IntegrationTest ++= environment.map { case (key, value) => s"-D$key=$value" }.toSeq,
      scalacOptions in (Compile, doc) ++= Seq("-groups", "-implicits"),
      assemblyJarName in assembly := s"$id.jar",
      assemblyMergeStrategy in assembly := {
        case PathList("javax", "servlet", xs @ _*)          => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".html"  => MergeStrategy.first
        case "application.conf"                             => MergeStrategy.concat
        case "io.netty.versions.properties"                 => MergeStrategy.concat
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      }
    )