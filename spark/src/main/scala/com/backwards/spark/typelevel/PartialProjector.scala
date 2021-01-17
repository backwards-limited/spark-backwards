package com.backwards.spark.typelevel

import shapeless._

trait PartialProjector[A, B] extends Serializable {
  def enforceNotNulls(a: A): Option[B]
}

object PartialProjector extends LowPriorityInstances {
  def instance[A, B](f: A => Option[B]): PartialProjector[A, B] =
    (a: A) => f(a)

  implicit def optionProjector[A]: PartialProjector[Option[A], A] = instance {
    case Some(x) => Option(x)
    case None => None
  }

  implicit def optOptProjector[A]: PartialProjector[Option[A], Option[A]] = instance {
    case Some(x) => if (x != null) Some(Some(x)) else Some(None)
    case None => Some(None)
  }

  implicit def hlistProjector[H, T <: HList, H1, T1 <: HList](
    implicit hProjector: PartialProjector[H, H1], tProjector: PartialProjector[T, T1]
  ): PartialProjector[H :: T, H1 :: T1] = instance {
    case h :: t => for {
      h0 <- hProjector.enforceNotNulls(h)
      t0 <- tProjector.enforceNotNulls(t)
    } yield h0 :: t0
  }

  /**
   * This function takes an implicit for generic representations of source and target ADTs (genA and genB).
   * The third generic is a projector to transform the generics, which will resolve to hlistProjector.
   * The method derives an HList from the input case object, runs the transformation on it and instantiates the target case class from the transformation result.
   */
  implicit def adtProjector[A, ReprA, B, ReprB](
    implicit genA: Generic.Aux[A, ReprA], genB: Generic.Aux[B, ReprB], projector: PartialProjector[ReprA, ReprB]
  ): PartialProjector[A, B] = instance(
    a => projector.enforceNotNulls(genA.to(a)).map(genB.from)
  )

  def apply[A, B](implicit partialProjector: PartialProjector[A, B]): PartialProjector[A, B] =
    partialProjector
}

trait LowPriorityInstances {
  /**
   * Lower priority to allow more specific projectors to be tried first.
   * Also, this can double up as a HNil projector
   */
  implicit def identityProjector[A]: PartialProjector[A, A] =
    (a: A) => Option(a)
}