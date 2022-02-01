package com.backwards.spark.ch5

import scala.concurrent.duration.FiniteDuration
import cats.data.StateT
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits._
import monocle.macros.GenLens
import monocle.{Lens, Optional}
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}
import com.backwards.spark.Spark.sparkResource
import com.backwards.spark.ch5.State._

final case class State(spark: SparkSession, dataset: Option[Dataset[Int]])

object State {
  val datasetL: Lens[State, Option[Dataset[Int]]] =
    GenLens[State](_.dataset)

  val datasetO: Optional[State, Dataset[Int]] =
    Optional[State, Dataset[Int]](_.dataset)(ds => datasetL.set(ds.some))
}

object PiComputeApp {
  def main(array: Array[String]): Unit =
    (for {
      sparkBefore <- Resource.eval(IO.realTime)
      spark <- sparkResource(_.appName("spark-pi").master("local[*]"))
      sparkAfter <- Resource.eval(IO.realTime)
      _ = println(s"1. Session initialized ........... ${sparkAfter - sparkBefore}")
    } yield
      State(spark, None)
    ).use(program.run).unsafeRunSync()

  def program: StateT[IO, State, Option[Dataset[Int]]] =
    for {
      dsBefore <- checkpoint
      slices = 10
      numberOfThrows = 100000 * slices
      _ = println(s"About to throw $numberOfThrows darts, ready? Stay away from the target!")
      _ <- dataset(numberOfThrows)
      dsAfter <- checkpoint
      _ = println(s"2. Initial dataset ............... ${dsAfter - dsBefore}")
      _ <- darts
      dartsAfter <- checkpoint
      _ = println(s"3. Throwing darts done ........... ${dartsAfter - dsAfter}")
      ds <- dartsInCircle
      dartsInCircleAfter <- checkpoint
      _ = println(s"4. Analysing results ............. ${dartsInCircleAfter - dartsAfter}")
      _ = ds.fold(println("ODD there were no darts in the circle"))( ds =>
        println(s"Pi is roughly: ${4.0 * ds / numberOfThrows}")
      )
      state <- StateT.get[IO, State]
    } yield
      state.dataset

  val checkpoint: StateT[IO, State, FiniteDuration] =
    StateT.liftF(IO.realTime)

  val dataset: Int => StateT[IO, State, Unit] =
    numberOfThrows => StateT.modify[IO, State](state =>
      datasetO.set(state.spark.createDataset(List.fill(numberOfThrows)(0))(Encoders.scalaInt))(state)
    )

  val darts: StateT[IO, State, Unit] = {
    val dart: ((Int, Long)) => Int = {
      case (_, index) =>
        if (index % 100000 == 0) println(s"$index darts thrown so far")

        val x = Math.random * 2 - 1
        val y = Math.random * 2 - 1

        if (x * x + y * y <= 1) 1 else 0
    }

    StateT.modifyF[IO, State](state =>
      IO {
        import state.spark.implicits._

        datasetO.modify(_.rdd.zipWithIndex().map(dart).toDS())(state)
      }
    )
  }

  val dartsInCircle: StateT[IO, State, Option[Int]] =
    StateT.get[IO, State].map(_.dataset.map(_.reduce(_ + _)))
}