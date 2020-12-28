package com.backwards.spark

import java.util.Properties
import scala.language.implicitConversions

object Collection {
  object syntax {
    implicit class Ops(m: Map[String, String]) {
      lazy val toProps: Properties =
        m.foldLeft(new Properties) { case (props, (k, v)) =>
          props.put(k, v)
          props
        }
    }
  }
}