package com.backwards.spark.typelevel

import scala.annotation.implicitNotFound
import shapeless.ops.hlist.ToTraversable
import shapeless.ops.record.Selector
import shapeless.{HList, HNil, LabelledGeneric, SingletonProductArgs, Witness}
import org.apache.spark.sql.{Column, DataFrame, Dataset, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * [[https://codeburst.io/type-safety-and-spark-datasets-in-scala-20fa582024fc Type safe Spark Datasets]]
 */
class ShapelessSpec extends AnyWordSpec with Matchers {
  lazy val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("test")
      .getOrCreate()
  }

  "" should {
    "" in {
      val generic = LabelledGeneric[Person]

      val person = Person("John Doe", 32, isEmployee = true)

      val hlist = generic.to(person)
      println(hlist)

      val personResolved: Person = generic.from(hlist)
      pprint.pprintln(personResolved)


      import spark.implicits._
      import PropertyExists.syntax._

      val personDs = List(person).toDS().enriched

      val ageColumn: Column = personDs('age) // compiles
      "val nameColumn: Column = personDs('namesss)" mustNot compile



      val datasetA = spark.createDataset(List(Blah("Scooby", "Doo")))
      val datasetB = spark.createDataset(List(Blah("Scrappy", "Doo")))

      // Desired DSL

      // For left join

      // Natural join single key reference
      datasetA.leftJoin(datasetB).withKey('firstName)

      // Natural join multiple keys
      datasetA.leftJoin(datasetB).on('firstName, 'lastName)

      // For joins not natural.
      /*datasetA.leftJoin(datasetB) where {
        datasetA('firstName) === datasetB('lastName)
      }
      TODO
      Fails compilation because of
      type mismatch;
      found   : Symbol
      required: String
        datasetA('firstName) === datasetB('lastName)
      */
    }
  }
}

final case class Person(name: String, age: Int, isEmployee: Boolean)

final case class Blah(firstName: String, lastName: String)

/**
 * Given a type T, if there exists a property of name PName and type PType then yes, the conditions are satisfied.
 */
@implicitNotFound(msg = "${PName} not found in ${T}")
trait PropertyExists[T, PName, PType]

object PropertyExists {
  def apply[T, PType](column: Witness)(
    implicit exists: PropertyExists[T, column.T, PType]
  ): PropertyExists[T, column.T, PType] =
    exists

  implicit def provider[T, H <: HList, PName, PType](
    implicit gen: LabelledGeneric.Aux[T, H],
    selector: Selector.Aux[H, PName, PType]
  ): PropertyExists[T, PName, PType] =
    new PropertyExists[T, PName, PType] {}

  object syntax {
    implicit class RichDataSet[A](dataSet: Dataset[A]) {
      // We need to expose enriched as the compile will pick apply method of Dataset and not that of RichDataset
      val enriched: RichDataSet[A] = this

      def apply[K](column: Witness.Lt[Symbol])(implicit exists: PropertyExists[A, column.T, K]): Column =
        new Column(column.value.name)

      def leftJoin[B, K](withDataSet: Dataset[B]): JoinDataset[A, B] =
        JoinDataset.leftJoin(dataSet, withDataSet)

      def rightJoin[B, K](withDataSet: Dataset[B]): JoinDataset[A, B] =
        JoinDataset.rightJoin(dataSet, withDataSet)

      def fullOuterJoin[B, K](withDataSet: Dataset[B]): JoinDataset[A, B] =
        JoinDataset.fullOuterJoin(dataSet, withDataSet)
    }
  }
}

@implicitNotFound(msg = "${PName} not found in ${T}")
trait PropertiesExists[T, PName <: HList, PType]

object PropertiesExists {
  import shapeless.::

  implicit def forHNil[T, PName, PType](
    implicit head: PropertyExists[T, PName, PType]
  ): PropertiesExists[T, PName :: HNil, PType] =
    new PropertiesExists[T, PName :: HNil, PType] {}

  implicit def forHList[T, PNameHead, PNameTail <: HList, PTypeForHead, PTypeForTail](
    implicit headExists: PropertyExists[T, PNameHead, PTypeForHead],
    tailExists: PropertiesExists[T, PNameTail, PTypeForTail]
  ): PropertiesExists[T, PNameHead :: PNameTail, PTypeForTail] =
    new PropertiesExists[T, PNameHead :: PNameTail, PTypeForTail] {}
}

class JoinDataset[L, R](lhs: Dataset[L], rhs: Dataset[R], joinType: String) {
  def doJoin(columns: Seq[String]): DataFrame =
    lhs.join(rhs, columns, joinType)

  def doJoin(column: Column): DataFrame =
    lhs.join(rhs, column, joinType)

  def withKey[K](column: Witness.Lt[Symbol])(
    implicit lhsExists: PropertyExists[L, column.T, K],
    rhsExists: PropertyExists[R, column.T, K]
  ): DataFrame =
    doJoin(Seq(column.value.name))

  def where(column: => Column): DataFrame =
    doJoin(column)

  object on extends SingletonProductArgs {
    def applyProduct[V <: HList, K](
      columns: V
    )(
      implicit i0: ToTraversable.Aux[V, List, Symbol],
      lhsExists: PropertiesExists[L, V, K],
      rhsExists: PropertiesExists[R, V, K]
    ): DataFrame =
      doJoin(columns.toList[Symbol].map(_.name))
  }
}

object JoinDataset {
  def apply[L, R](lhs: Dataset[L], rhs: Dataset[R], joinType: String): JoinDataset[L, R] =
    new JoinDataset(lhs, rhs, joinType)

  def leftJoin[L, R](lhs: Dataset[L], rhs: Dataset[R]): JoinDataset[L, R] =
    JoinDataset(lhs, rhs, "leftOuter")

  def rightJoin[L, R](lhs: Dataset[L], rhs: Dataset[R]): JoinDataset[L, R] =
    JoinDataset(lhs, rhs, "rightOuter")

  def fullOuterJoin[L, R](lhs: Dataset[L], rhs: Dataset[R]): JoinDataset[L, R] =
    JoinDataset(lhs, rhs, "fullOuter")
}