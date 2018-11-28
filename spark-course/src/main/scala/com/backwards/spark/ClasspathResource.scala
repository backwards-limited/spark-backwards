package com.backwards.spark

import java.nio.file.Paths
import scala.language.postfixOps

case class ClasspathResource private(value: String) {
  lazy val absolutePath: String =
    Paths.get(getClass getResource value toURI).toAbsolutePath.toString
}

object ClasspathResource {
  def apply(resource: String): ClasspathResource =
    if (resource.startsWith("/")) new ClasspathResource(resource)
    else new ClasspathResource(s"/$resource")
}