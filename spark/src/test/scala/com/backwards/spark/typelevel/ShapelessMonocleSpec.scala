package com.backwards.spark.typelevel

import java.net.URL
import java.time.{LocalDate, ZonedDateTime}
import java.util.Currency
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

import java.time.{Clock, DayOfWeek, ZonedDateTime}

/**
 * [[https://ivan-kurchenko.medium.com/complex-test-data-prototyping-with-shapeless-and-monocle-cfe9d271d7fc Complex test data prototyping with Shapeless and Monocle]]
 *
 * The application calculates the total amount of money spent on restaurants in the workweek during the current and previous month.
 * If those values differ by more than 75%, we can suggest optimizing expenses in this category.
 * For simplicity, let’s say we generate this report once per month.
 */
class ShapelessMonocleSpec extends AnyWordSpec with Matchers {
  "" should {
    "" in {

    }
  }
}

trait ShapelessMonocleFixture {
  import CategoryTags.CategoryTag
  import ExpenseStatuses.ExpenseStatus

  object ExpenseStatuses extends Enumeration {
    type ExpenseStatus = Value

    val Completed = Value("completed")
    val Rejected = Value("rejected")
    val Canceled = Value("canceled")
  }

  final case class Address(
    country: String,
    city: String,
    street: String,
    building: Int
  )

  final case class Card(
    id: Long,
    number: String,
    expires: LocalDate,
    holder: String,
    bank: String,
    currency: String,
    credit: Boolean
  )

  object CategoryTags extends Enumeration {
    type CategoryTag = Value

    val Restaurants = Value("restaurants")
    val Entertainment = Value("entertainment")
    val Movies = Value("movies")
    val Transport = Value("transport")
    val Grocery = Value("grocery")
    val Clothing = Value("clothing")
  }

  final case class Category(
    id: Long,
    name: String,
    tags: Set[CategoryTag]
  )

  final case class Amount(
    banknotes: Int,
    coins: Int,
    currency: String
  )

  final case class Expense(
    id: Long,
    dateTime: String,
    userId: Long,
    card: Card,
    place: Address,
    status: ExpenseStatus,
    category: Category,
    spend: Amount,
    exchangeRate: Option[Float],
    debit: Amount,
    website: Option[String]
  )
}