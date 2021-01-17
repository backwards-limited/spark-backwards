package com.backwards.spark.typelevel

import shapeless._
import shapeless.ops.hlist.ToTraversable
import shapeless.ops.record.Keys

/**
 * [[https://svejcar.dev/posts/2019/10/22/extracting-case-class-field-names-with-shapeless/ Extracting case class field names with Shapeless]]
 */
trait Attributes[T] {
  def fieldNames: List[String]
}

object Attributes {
  implicit def toAttributes[T, Repr <: HList, KeysRepr <: HList](
    implicit gen: LabelledGeneric.Aux[T, Repr],
    keys: Keys.Aux[Repr, KeysRepr],
    traversable: ToTraversable.Aux[KeysRepr, List, Symbol]
  ): Attributes[T] = new Attributes[T] {
    override def fieldNames: List[String] =
      keys().toList.map(_.name)
  }

  def apply[T: Attributes]: Attributes[T] =
    implicitly[Attributes[T]]
}