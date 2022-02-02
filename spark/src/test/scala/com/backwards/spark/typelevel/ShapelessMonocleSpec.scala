package com.backwards.spark.typelevel

import java.time._
import scala.annotation.implicitNotFound
import cats.Id
import cats.data.Reader
import monocle.macros.GenLens
import shapeless._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.spark.typelevel.CategoryTags.CategoryTag
import com.backwards.spark.typelevel.ExpenseStatuses.ExpenseStatus
import com.backwards.spark.typelevel.Prototype._

/**
 * [[https://ivan-kurchenko.medium.com/complex-test-data-prototyping-with-shapeless-and-monocle-cfe9d271d7fc Complex test data prototyping with Shapeless and Monocle]]
 *
 * The application calculates the total amount of money spent on restaurants in the workweek during the current and previous month.
 * If those values differ by more than 75%, we can suggest optimizing expenses in this category.
 * For simplicity, let’s say we generate this report once per month.
 */
class ShapelessMonocleSpec extends AnyWordSpec with Matchers {
  "Restaurants Optimization Recommendation" should {
    "produce recommendation for user #1 - iteration 1 with boilerplate" in {
      val usd: String = "USD"
      val zone: ZoneId = ZoneId.of("America/Chicago")

      val clock: Clock = {
        val instant = LocalDate.of(2021, 12, 15).atStartOfDay().toInstant(ZoneOffset.UTC)
        Clock.fixed(instant, zone)
      }

      val card: Card =
        Card(
          id = 1,
          number = "1234 5678 9012 3456",
          expires = LocalDate.of(2022, 12, 1),
          holder = "John Doe",
          bank = "Bank of Earth",
          currency = usd,
          credit = true
        )

      val restaurantAddress: Address =
        Address(
          country = "USA",
          city = "Chicago",
          street = "Michigan",
          building = 1
        )

      val groceryAddress: Address =
        Address(
          country = "USA",
          city = "Chicago",
          street = "Michigan",
          building = 2
        )

      val restaurantCategory: Category =
        Category(1, "Restaurants", Set(CategoryTags.Restaurants, CategoryTags.Entertainment))

      val groceriesCategory: Category =
        Category(2, "Groceries", Set(CategoryTags.Grocery))

      val expenses: List[Expense] =
        List(
          // User #1 spend spend 10$ in restaurant at Monday 8 of November 2021.
          Expense(
            id = 0,
            dateTime = ZonedDateTime.of(2021, 11, 8, 13, 0, 0, 0, zone).toString,
            userId = 1,
            card = card,
            place = restaurantAddress,
            status = ExpenseStatuses.Completed,
            category = restaurantCategory,
            spend = Amount(10, 0, usd),
            exchangeRate = None,
            debit = Amount(10_000, 0, usd),
            website = None
          ),

          // User #1 spend spend 50$ in grocery at Sunday 13 of November 2021.
          Expense(
            id = 0,
            dateTime = ZonedDateTime.of(2021, 11, 13, 10, 0, 0, 0, zone).toString,
            userId = 1,
            card = card,
            place = restaurantAddress,
            status = ExpenseStatuses.Completed,
            category = groceriesCategory,
            spend = Amount(50, 0, usd),
            exchangeRate = None,
            debit = Amount(11_000, 0, usd),
            website = None
          ),

          // User #1 spend spend 20$ in restaurant at Monday 6 of December 2021.
          Expense(
            id = 0,
            dateTime = ZonedDateTime.of(2021, 12, 6, 13, 0, 0, 0, zone).toString,
            userId = 1,
            card = card,
            place = restaurantAddress,
            status = ExpenseStatuses.Completed,
            category = restaurantCategory,
            spend = Amount(20, 0, usd),
            exchangeRate = None,
            debit = Amount(12_000, 0, usd),
            website = None
          ),

          // User #1 spend spend 200$ in grocery at Tuesday 7 of December 2021.
          Expense(
            id = 0,
            dateTime = ZonedDateTime.of(2021, 12, 7, 10, 0, 0, 0, zone).toString,
            userId = 1,
            card = card,
            place = restaurantAddress,
            status = ExpenseStatuses.Completed,
            category = groceriesCategory,
            spend = Amount(200, 0, usd),
            exchangeRate = None,
            debit = Amount(8_000, 0, usd),
            website = None
          )
        )

      val localSession: SparkSession =
        SparkSession.builder().appName("test").master("local[1]").getOrCreate()

      import localSession.implicits._

      val recommendation: RestaurantsOptimizationRecommendation =
        new RestaurantsOptimizationRecommendation(clock)

      val recommendations: Id[Dataset[ExpenseRecommendation]] =
        recommendation.recommendation(expenses.toDS).run(localSession)

      recommendations.count() mustBe 1L

      val actualRecommendation: ExpenseRecommendation =
        recommendations.head(1).head

      val expectedRecommendation: ExpenseRecommendation =
        ExpenseRecommendation(1, "Your expenses on restaurants on workdays increased on 100.0%")

      actualRecommendation mustBe expectedRecommendation

      /*
      Mmmmm.
      We wrote a lot of Expense objects
      */
    }

    "produce recommendation for user #1 - iteration 2 refactor to Null object pattern" in {
      // In object-oriented computer programming, a null object is an object with no referenced value or with defined neutral (null) behavior.

      val usd: String = "USD"
      val zone: ZoneId = ZoneId.of("America/Chicago")

      val clock: Clock = {
        val instant = LocalDate.of(2021, 12, 15).atStartOfDay().toInstant(ZoneOffset.UTC)
        Clock.fixed(instant, zone)
      }

      val card: Card =
        Card(
          id = 1,
          number = "1234 5678 9012 3456",
          expires = LocalDate.of(2022, 12, 1),
          holder = "John Doe",
          bank = "Bank of Earth",
          currency = usd,
          credit = true
        )

      val addressPrototype: Address =
        Address(
          country = "USA",
          city = "Chicago",
          street = "Michigan",
          building = 1
        )

      val restaurantAddress: Address =
        addressPrototype.copy(building = 1)

      val groceryAddress: Address =
        addressPrototype.copy(building = 2)

      val categoryPrototype: Category =
        Category(1, "", Set.empty)

      val restaurantCategory: Category =
        Category(1, "Restaurants", Set(CategoryTags.Restaurants, CategoryTags.Entertainment))

      val groceriesCategory: Category =
        Category(2, "Groceries", Set(CategoryTags.Grocery))

      val amountPrototype: Amount =
        Amount(0, 0, usd)

      val expensePrototype: Expense =
        Expense(
          id = 0,
          dateTime = "",
          userId = 1,
          card = card,
          place = restaurantAddress,
          status = ExpenseStatuses.Completed,
          category = categoryPrototype,
          spend = amountPrototype,
          exchangeRate = None,
          debit = amountPrototype,
          website = None
        )

      val expenses: List[Expense] =
        List(
          // User #1 spend spend 10$ in restaurant at Monday 8 of November 2021.
          expensePrototype.copy(
            dateTime = ZonedDateTime.of(2021, 11, 8, 13, 0, 0, 0, zone).toString,
            category = restaurantCategory,
            spend = amountPrototype.copy(banknotes = 10),
          ),

          // User #1 spend spend 50$ in grocery at Sunday 13 of November 2021.
          expensePrototype.copy(
            dateTime = ZonedDateTime.of(2021, 11, 13, 10, 0, 0, 0, zone).toString,
            category = groceriesCategory,
            spend = amountPrototype.copy(banknotes = 50),
          ),

          // User #1 spend spend 20$ in restaurant at Monday 6 of December 2021.
          expensePrototype.copy(
            dateTime = ZonedDateTime.of(2021, 12, 6, 13, 0, 0, 0, zone).toString,
            category = restaurantCategory,
            spend = amountPrototype.copy(banknotes = 20),
          ),

          // User #1 spend spend 200$ in grocery at Tuesday 7 of December 2021.
          expensePrototype.copy(
            dateTime = ZonedDateTime.of(2021, 12, 7, 10, 0, 0, 0, zone).toString,
            category = groceriesCategory,
            spend = amountPrototype.copy(banknotes = 200),
          )
        )

      val localSession: SparkSession =
        SparkSession.builder().appName("test").master("local[1]").getOrCreate()

      import localSession.implicits._

      val recommendation: RestaurantsOptimizationRecommendation =
        new RestaurantsOptimizationRecommendation(clock)

      val recommendations: Id[Dataset[ExpenseRecommendation]] =
        recommendation.recommendation(expenses.toDS).run(localSession)

      recommendations.count() mustBe 1L

      val actualRecommendation: ExpenseRecommendation =
        recommendations.head(1).head

      val expectedRecommendation: ExpenseRecommendation =
        ExpenseRecommendation(1, "Your expenses on restaurants on workdays increased on 100.0%")

      actualRecommendation mustBe expectedRecommendation
    }

    "produce recommendation for user #1 - iteration 3 from prototyping to Shapeless and Monocle" in {
      // Prototype instantiation remains tedious, while this is writing simple code, which can be generated for us.
      // Shapeless type classes derivation capabilities can help to solve this task

      // Using copy is not composable: if we want to apply the same transformation but for different objects, we would need to repeat the same copy invocation.
      // Answer to this problem - optics, Monocle in particular.

      // In order to instantiate any case class prototype, we need to instantiate it with some “default” values, such "" for string, 0 for integers, None for options, and so on.
      // Shapeless is a perfect tool to solve this. Generating such prototype instance sounds like similar deriving type class, which Shapeless perfectly does.
      // In our case, Prototype type class should return the prototype value.

      // Domain specific implicits should be outside default `Prototype` implicits.
      implicit val expenseStatusPrototype: Prototype[ExpenseStatus] =
        new Prototype[ExpenseStatus](ExpenseStatuses.Completed)

      val usd: String = "USD"
      val zone: ZoneId = ZoneId.of("America/Chicago")

      val clock: Clock = {
        val instant = LocalDate.of(2021, 12, 15).atStartOfDay().toInstant(ZoneOffset.UTC)
        Clock.fixed(instant, zone)
      }

      val novemberMonday: String =
        ZonedDateTime.of(2021, 11, 8, 13, 0, 0, 0, zone).toString

      val novemberSunday: String =
        ZonedDateTime.of(2021, 11, 13, 10, 0, 0, 0, zone).toString

      val decemberMonday: String =
        ZonedDateTime.of(2021, 12, 7, 10, 0, 0, 0, zone).toString

      val restaurantCategory: Category =
        Category(1, "Restaurants", Set(CategoryTags.Restaurants, CategoryTags.Entertainment))

      val groceriesCategory: Category =
        Category(2, "Groceries", Set(CategoryTags.Grocery))

      object amounts {
        val usd: Amount => Amount =
          GenLens[Amount](_.currency).set("usd")

        val ten: Amount => Amount =
          GenLens[Amount](_.banknotes).set(10)

        val twenty: Amount => Amount =
          GenLens[Amount](_.banknotes).set(20)

        val fifty: Amount => Amount =
          GenLens[Amount](_.banknotes).set(50)

        val twoHundred: Amount => Amount =
          GenLens[Amount](_.banknotes).set(200)

        val tenUsd: Amount =
          (usd compose ten).prototype // SAME AS: (usd compose ten)(prototype[Amount])

        val twentyUsd: Amount =
          (usd compose twenty).prototype

        val fiftyUsd: Amount =
          (usd compose fifty).prototype

        val twoHundredUsd: Amount =
          (usd compose twoHundred).prototype
      }

      object expenses {
        val user1: Expense => Expense =
          GenLens[Expense](_.userId).set(1)

        val restaurant: Expense => Expense =
          GenLens[Expense](_.category).set(restaurantCategory)

        val grocery: Expense => Expense =
          GenLens[Expense](_.category).set(groceriesCategory)

        val tenUsd: Expense => Expense =
          GenLens[Expense](_.spend).set(amounts.tenUsd)

        val twentyUsd: Expense => Expense =
          GenLens[Expense](_.spend).set(amounts.twentyUsd)

        val fiftyUsd: Expense => Expense =
          GenLens[Expense](_.spend).set(amounts.fiftyUsd)

        val twoHundredUsd: Expense => Expense =
          GenLens[Expense](_.spend).set(amounts.twoHundredUsd)

        val novemberWorkday: Expense => Expense =
          GenLens[Expense](_.dateTime).set(novemberMonday)

        val novemberWeekend: Expense => Expense =
          GenLens[Expense](_.dateTime).set(novemberSunday)

        val decemberWorkday: Expense => Expense =
          GenLens[Expense](_.dateTime).set(decemberMonday)
      }

      val expensesData: List[Expense] = {
        import expenses._

        List(
          (user1 compose novemberWorkday compose restaurant compose tenUsd).prototype,
          (user1 compose novemberWorkday compose grocery compose tenUsd).prototype,
          (user1 compose novemberWeekend compose restaurant compose fiftyUsd).prototype,

          (user1 compose decemberWorkday compose restaurant compose twentyUsd).prototype,
          (user1 compose decemberWorkday compose grocery compose twoHundredUsd).prototype,
        )
      }

      val localSession: SparkSession =
        SparkSession.builder().appName("test").master("local[1]").getOrCreate()

      import localSession.implicits._

      val recommendation: RestaurantsOptimizationRecommendation =
        new RestaurantsOptimizationRecommendation(clock)

      val recommendations: Id[Dataset[ExpenseRecommendation]] =
        recommendation.recommendation(expensesData.toDS).run(localSession)

      recommendations.count() mustBe 1L

      val actualRecommendation: ExpenseRecommendation =
        recommendations.head(1).head

      val expectedRecommendation: ExpenseRecommendation =
        ExpenseRecommendation(1, "Your expenses on restaurants on workdays increased on 100.0%")

      actualRecommendation mustBe expectedRecommendation
    }
  }
}

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

final case class ExpenseRecommendation(userId: Long, recommendation: String)

final case class MonthExpenses(userId: Long, currentMonth: Boolean, spend: Int)

class RestaurantsOptimizationRecommendation(clock: Clock) {
  /**
   * Generate recommendation per user to optimize expenses on restaurants during work week and consider options
   * such as groceries and cooking at home. Restaurant expenses are those which contains `CategoryTags.Restaurants` in it.
   */
  //def recommendation(expenses: Dataset[Expense])(implicit session: SparkSession): Dataset[ExpenseRecommendation] = {
  def recommendation(expenses: Dataset[Expense]): Reader[SparkSession, Dataset[ExpenseRecommendation]] =
    Reader { sparkSession =>
      import sparkSession.implicits._

      val today: ZonedDateTime =
        ZonedDateTime.now(clock)

      val currentMonthStart: ZonedDateTime =
        today.withDayOfMonth(1)

      val previousMonth: ZonedDateTime =
        today.minusMonths(1)

      val previousMonthStart: ZonedDateTime =
        previousMonth.withDayOfMonth(1)

      val weekDayRestaurantExpenses: Dataset[MonthExpenses] =
        expenses
          .filter(_.status == ExpenseStatuses.Completed)
          .filter(_.category.tags.contains(CategoryTags.Restaurants))
          .filter(expense => ZonedDateTime.parse(expense.dateTime).isAfter(previousMonthStart))
          .filter { expense =>
            val dateTime: ZonedDateTime =
              ZonedDateTime.parse(expense.dateTime)

            val dayOfWeek: DayOfWeek =
              dateTime.getDayOfWeek

            dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
          }
          .map { expense =>
            val dateTime: ZonedDateTime =
              ZonedDateTime.parse(expense.dateTime)

            val userId: Long =
              expense.userId

            val currentMonth: Boolean =
              dateTime.isAfter(currentMonthStart)

            val spend: Int =
              expense.spend.banknotes * 100 + expense.spend.coins // let's take simple model when coin is 1% of banknote nominal

            val exchangeSpend: Int =
              expense.exchangeRate.fold(spend)(rate => Math.round(spend * rate))

            MonthExpenses(userId, currentMonth, exchangeSpend)
          }

      val currentMonthTotal: DataFrame =
        weekDayRestaurantExpenses
          .filter(_.currentMonth)
          .groupBy("userId")
          .sum("spend")
          .withColumnRenamed("sum(spend)", "spend")

      val previousMonthTotal: DataFrame =
        weekDayRestaurantExpenses
          .filter(!_.currentMonth)
          .groupBy("userId")
          .sum("spend")
          .withColumnRenamed("sum(spend)", "spend")

      currentMonthTotal
        .join(previousMonthTotal, "userId")
        .withColumn("delta", ((currentMonthTotal("spend") / previousMonthTotal("spend")) * lit(100)) - lit(100))
        .filter(col("delta") > 75)
        .select(currentMonthTotal("userId"), col("delta"))
        .withColumn("recommendation", concat(lit("Your expenses on restaurants on workdays increased on "), col("delta"), lit("%")))
        .as[ExpenseRecommendation]
    }
}

/**
 * Type class to generate test data prototype
 * @tparam T prototype
 */
@implicitNotFound("Cannot resolve prototype of the type ${T}")
class Prototype[T](val value: T)

trait PrototypeLowPriority {
  implicit val deriveHNil: Prototype[HNil] =
    new Prototype(HNil)

  implicit def deriveHCons[V, T <: HList](implicit sv: => Prototype[V], st: Prototype[T]): Prototype[V :: T] =
    new Prototype(sv.value :: st.value)

  implicit def deriveInstance[V, R](implicit gen: Generic.Aux[V, R], sr: => Prototype[R]): Prototype[V] =
    new Prototype(gen.from(sr.value))
}

object Prototype extends PrototypeLowPriority {
  implicit val prototypeString: Prototype[String] =
    new Prototype("")

  implicit val prototypeInt: Prototype[Int] =
    new Prototype(0)

  implicit val protptypeLong: Prototype[Long] =
    new Prototype(0)

  implicit val prototypeFloat: Prototype[Float] =
    new Prototype(0)

  implicit val prototypeDouble: Prototype[Double] =
    new Prototype(0)

  implicit val prototypeBoolean: Prototype[Boolean] =
    new Prototype(false)

  implicit val prototypeLocalDate: Prototype[LocalDate] =
    new Prototype[LocalDate](LocalDate.ofYearDay(1970, 1))

  implicit val prototypeZonedDateTime: Prototype[ZonedDateTime] =
    new Prototype[ZonedDateTime](ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()))

  implicit def prototypeOption[T]: Prototype[Option[T]] =
    new Prototype(None)

  implicit def prototypeSet[T]: Prototype[Set[T]] =
    new Prototype(Set.empty)

  implicit def prototypeList[T]: Prototype[List[T]] =
    new Prototype(List.empty)

  def apply[T: Prototype]: Prototype[T] =
    implicitly

  def prototype[T: Prototype]: T =
    Prototype[T].value

  implicit class LensPrototypeSyntax[T: Prototype](lens: T => T) {
    val prototype: T =
      lens(Prototype[T].value)
  }
}

/*
trait PrototypeSyntax {
  def prototype[T: Prototype]: T =
    Prototype[T].value

  // This piece of syntax is for monocle and we will need it later
  implicit class LensPrototypeSyntax[T](lens: T => T) {
    def prototype(implicit p: Prototype[T]): T = lens(p.value)
  }
}

package object prototype extends PrototypeSyntax
*/
