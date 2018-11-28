package com.backwards.spark

object Delimiter {
  // A regular expression which matches commas, but not commas within double quotations
  val comma = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"

  val tab = "\t"
}