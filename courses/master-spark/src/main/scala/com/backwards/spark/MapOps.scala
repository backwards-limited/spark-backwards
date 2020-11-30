package com.backwards.spark

import java.util.Properties
import scala.language.implicitConversions

object MapOps {
  implicit def map2Properties(m: Map[String, String]): Properties =
    m.foldLeft(new Properties) { case (props, (k, v)) =>
      props.put(k, v)
      props
    }
}