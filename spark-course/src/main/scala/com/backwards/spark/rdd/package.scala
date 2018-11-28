package com.backwards.spark

import scala.reflect.ClassTag
import org.apache.spark.rdd.RDD

package object rdd {
  def dropFirstLine[T: ClassTag](rdd: RDD[T]): RDD[T] =
    rdd mapPartitionsWithIndex { (index, iterator) =>
      if (index == 0) iterator.drop(1) else iterator
    }
}