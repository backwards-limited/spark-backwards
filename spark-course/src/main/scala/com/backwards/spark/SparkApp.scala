package com.backwards.spark

import wvlet.log.LogSupport
import org.apache.log4j.{Level, Logger}

trait SparkApp extends App with LogSupport {
  Logger getLogger "org" setLevel Level.ERROR
}