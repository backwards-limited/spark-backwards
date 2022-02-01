package com.backwards.spark.ch4

import scala.concurrent.duration.FiniteDuration
import better.files.Resource.getUrl
import cats.data.StateT
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits._
import monocle.Lens
import monocle.macros.GenLens
import org.apache.spark.sql.functions.{col, expr}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import com.backwards.spark.Spark.sparkResource
import com.backwards.spark.ch4.State._

final case class State(spark: SparkSession, dataset: Dataset[Row])

object State {
  val datasetL: Lens[State, Dataset[Row]] =
    GenLens[State](_.dataset)
}

object TransformationAndActionApp {
  def main(array: Array[String]): Unit =
    (for {
      sparkBefore <- Resource.eval(IO.realTime)
      spark <- sparkResource(_.appName("transformation-and-action").master("local[*]"))
      sparkAfter <- Resource.eval(IO.realTime)
      _ = println(s"1. Creating a session ........... ${sparkAfter - sparkBefore}")
    } yield
      State(spark, spark.emptyDataFrame)
    ).use(program.run).unsafeRunSync()

  def program: StateT[IO, State, Dataset[Row]] =
    for {
      dsBefore <- checkpoint
      _ <- teenBirthRates
      dsAfter <- checkpoint
      _ = println(s"2. Loading initial dataset ...... ${dsAfter - dsBefore}")
      _ <- StateT.modify[IO, State](datasetL modify teenBirthRatesUnion)
      dsUnionAfter <- checkpoint
      _ = println(s"3. Building full dataset ........ ${dsUnionAfter - dsAfter}")
      _ <- StateT.modify[IO, State](datasetL modify teenBirtRatesCleanup)
      dsCleanupAfter <- checkpoint
      _ = println(s"4. Clean-up ..................... ${dsCleanupAfter - dsUnionAfter}")
      _ <- StateT.modify[IO, State](datasetL modify teenBirthRatesTransform("full"))
      dsTransformAfter <- checkpoint
      _ = println(s"5. Transformations  ............. ${dsTransformAfter - dsCleanupAfter}")
      state <- StateT.get[IO, State]
      ds = state.dataset.collect()
      dsFinal <- checkpoint
      _ = println(s"6. Final action ................. ${dsFinal - dsTransformAfter}")
      _ = println(s"# of records .................... ${ds.length}")
    } yield
      state.dataset

  val checkpoint: StateT[IO, State, FiniteDuration] =
    StateT.liftF(IO.realTime)

  val teenBirthRates: StateT[IO, State, Unit] =
    StateT.modifyF[IO, State](state =>
      IO(datasetL.set(state.spark.read.format("csv").option("header", "true").load(getUrl("ch4/nchs-teen-birth-rates-for-age-group-15-19-in-united-states-by-county.csv").getFile))(state))
    )

  val teenBirthRatesUnion: Dataset[Row] => Dataset[Row] =
    ds => (0 to 60).foldLeft(ds)((dsAcc, _) => dsAcc union ds)

  val teenBirtRatesCleanup: Dataset[Row] => Dataset[Row] =
    _.withColumnRenamed("Lower Confidence Limit", "lcl").withColumnRenamed("Upper Confidence Limit", "ucl")

  val teenBirthRatesTransform: String => Dataset[Row] => Dataset[Row] = {
    val transform: Dataset[Row] => Dataset[Row] =
      _.withColumn("avg", expr("(lcl + ucl) / 2"))
       .withColumn("lcl2", col("lcl"))
       .withColumn("ucl2", col("ucl"))

    val drop: Dataset[Row] => Dataset[Row] =
      _.drop("avg","lcl2","ucl2")

    {
      case "full" => transform andThen drop
      case "col" => transform
      case _ => identity
    }
  }
}