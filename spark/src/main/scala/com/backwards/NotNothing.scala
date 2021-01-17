package com.backwards

import scala.annotation.implicitNotFound

@implicitNotFound("Sorry, type inference was unable to figure out the type. You need to provide it explicitly.")
trait NotNothing[T]

object NotNothing {
  private val evidence: NotNothing[Any] = new Object with NotNothing[Any]

  implicit def notNothingEv[T](implicit n: T =:= T): NotNothing[T] = evidence.asInstanceOf[NotNothing[T]]
}