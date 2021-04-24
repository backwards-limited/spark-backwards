package com.backwards.spark.fp

/*import scala.concurrent.duration._
import scala.util.Random
import cats._
import cats.data._
import cats.effect.{IO, Resource}
import cats.implicits._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.backwards.spark.SparkDeprecated._*/

/**
 * [[https://iravid.com/posts/fp-and-spark.html Functional Programming and Spark]]
 */
// TODO - Is Monix up to date with Cats Effect 3 ?
class FPSpec /*extends AnyWordSpec with Matchers {
  "Spark" should {
    "IO (one way)" in {
      def program(spark: SparkSession): IO[Row] = {
        import spark.implicits._

        IO.delay {
          val df: DataFrame =
            spark.sparkContext.parallelize(List.fill(100)(Random.nextLong)).toDF

          df.agg(avg("value")).head
        }
      }

      val result: Resource[IO, Row] = for {
        spark <- sparkSession(_.appName("test").master("local"))
        result <- Resource.eval(program(spark))
      } yield result

      import cats.effect.unsafe.implicits.global

      println(result.use(_.pure[IO]).unsafeRunSync())
    }

    "IO (another way)" in {
      val result: IO[Row] = sparkSession(_.appName("test").master("local")).use { spark =>
        import spark.implicits._

        IO {
          val df: DataFrame =
            spark.sparkContext.parallelize(List.fill(100)(Random.nextLong)).toDF

          df.agg(avg("value")).head
        }
      }

      import cats.effect.unsafe.implicits.global

      println(result.unsafeRunSync())
    }
  }

  "Spark with Monix" should {
    "task" in {
      def buildSession: Task[SparkSession] = Task {
        SparkSession
          .builder
          .appName("test")
          .master("local")
          .config("spark.scheduler.mode", "FAIR")
          .getOrCreate
      }

      // TODO - CHANGE TO KLEISLI INSTEAD OF IMPLICIT
      def createDF(data: List[Int])(implicit session: SparkSession): Task[DataFrame] = Task {
        import session.implicits._

        val rdd = session.sparkContext.parallelize(data)

        rdd.toDF
      }

      def computeAvg(df: DataFrame, pool: String)(implicit session: SparkSession): Task[Double] = Task {
        session.sparkContext.setLocalProperty("spark.scheduler.pool", pool)
        val result = df.agg(avg("value")).head().getDouble(0)
        session.sparkContext.setLocalProperty("spark.scheduler.pool", null)

        result
      }

      def program: Task[Double] = for {
        sparkSession <- buildSession
        result <- {
          implicit val session: SparkSession = sparkSession

          val data = List.fill(100)(Random.nextInt)

          for {
            df <- createDF(data)
            avg <- computeAvg(df, "pool")
          } yield avg
        }
      } yield result

      println(program.runSyncUnsafe())
    }

    "timed task with WriterT" in {
      case class Timings(data: Map[String, FiniteDuration])

      type TimedTask[A] = WriterT[Task, Timings, A]

      /**
       * When we compose the tasks together using for comprehensions, the Writer monad will concatenate the maps to keep the timings.
       * For that to work, we need a Monoid instance for Timings.
       */
      implicit val timingsMonoid: Monoid[Timings] = new Monoid[Timings] {
        def empty: Timings = Timings(Map.empty)

        def combine(x: Timings, y: Timings): Timings = Timings(x.data ++ y.data)
      }

      /**
       * Let’s add a combinator to lift a Task[A] to TimedTask[A].
       * The combinator will measure the time before and after the task and add that entry to the Timings map.
       * We’ll also add a combinator that marks a task as untimed.
       */
      implicit class TaskOps[A](task: Task[A]) {
        def onPool(pool: String)(implicit session: SparkSession): Task[A] =
          Task(session.sparkContext.setLocalProperty("spark.scheduler.pool", pool))
            .flatMap(_ => task)
            .doOnFinish(_ => Task(session.sparkContext.setLocalProperty("spark.scheduler.pool", null)))

        def timed(key: String): TimedTask[A] =
          WriterT(
            for {
              startTime <- Task.eval(System.currentTimeMillis().millis)
              result <- task
              endTime <- Task.eval(System.currentTimeMillis().millis)
            } yield (Timings(Map(key -> (endTime - startTime))), result)
          )

        def untimed: TimedTask[A] =
          WriterT(task.map((Monoid[Timings].empty, _)))
      }

      def buildSession: Task[SparkSession] = Task {
        SparkSession
          .builder
          .appName("test")
          .master("local")
          .config("spark.scheduler.mode", "FAIR")
          .getOrCreate
      }

      // TODO - CHANGE TO KLEISLI INSTEAD OF IMPLICIT
      def createDF(data: List[Int])(implicit session: SparkSession): Task[DataFrame] = Task {
        import session.implicits._

        val rdd = session.sparkContext.parallelize(data)

        rdd.toDF
      }

      def computeAvg(df: DataFrame)(implicit session: SparkSession): Task[Double] = Task(
        df.agg(avg("value")).head().getDouble(0)
      )

      def program(data: List[Int]): TimedTask[Double] =
        for {
          implicit0(spark: SparkSession) <- WriterT.liftF[Task, Timings, SparkSession](buildSession)
          df <- createDF(data).timed("DataFrame creation")
          avg <- computeAvg(df).onPool("pool").timed("Average computation")
        } yield avg

      val (timings: Timings, result: Double) =
        program(List.fill(100)(Random.nextInt)).run.runSyncUnsafe()

      pprint.pprintln(timings)
      pprint.pprintln(result)
    }

    /**
     * StateT[F[_], S, A] is isomorphic to S => F[(S, A)]
     *  - a function that receives an initial state and outputs a resulting state with a resulting value in an effect F.
     */
    "task with StateT" in {
      // Similarly to how we worked with the WriterT monad, we first define the state that we use in our program, and partially apply the State monad along with it:
      final case class JobState(keys: List[String], df: DataFrame)

      type StateAction[A] = StateT[Task, JobState, A]

      val loadKeys: StateAction[Unit] = StateT.modifyF { s =>
        Task(s.copy(keys = List.fill(10)(Random.nextString(5))))
      }

      val pruneSomeKeys: StateAction[Unit] = StateT.modifyF { s =>
        Task(s.copy(keys = s.keys take 3))
      }

      val pruneMoreKeys: StateAction[Unit] = StateT.modifyF { s =>
        Task(s.copy(keys = s.keys drop 1))
      }

      def createDF(implicit spark: SparkSession): StateAction[Unit] = {
        import spark.implicits._

        StateT.modifyF { s =>
          Task(s.copy(df = s.keys.toDF))
        }
      }

      def transformDF(implicit spark: SparkSession): StateAction[Unit] =
        StateT.modifyF { s =>
          Task(s.copy(df = s.df limit 3))
        }

      def buildSession: Task[SparkSession] = Task {
        SparkSession
          .builder
          .appName("test")
          .master("local")
          .config("spark.scheduler.mode", "FAIR")
          .getOrCreate
      }

      def program(implicit spark: SparkSession): StateAction[Unit] = for {
        _ <- loadKeys
        _ <- pruneSomeKeys
        _ <- pruneMoreKeys
        _ <- createDF
        _ <- transformDF
      } yield ()

      val (jobState: JobState, _) =
        buildSession.flatMap(spark => program(spark).run(JobState(Nil, spark.sqlContext.emptyDataFrame))).runSyncUnsafe()

      println(jobState)
    }

    "task with IndexedStateT as an improvement" in {
      // The only issue that we might take with the above design is that we shoved all the data into the state, while the keys aren’t needed when running transformDF.
      // Additionally, we had to introduce an artificial empty state; this goes against a good practice of making illegal states unrepresentable.

      // We can use IndexedStateT to model this more accurately; this is a data type similar to StateT that differs by having different types for input and output states.
      // Formally, it is a function of the form
      // SA => F[(SB, A)]
      // where SA and SB represent the input and output states.

      // To use it, we’ll define separate states for our program:

      final case object Empty

      final case class ProcessingKeys(keys: List[String])

      final case class ProcessingDF(df: DataFrame)

      final case class Done(df: DataFrame)

      // We will redefine our functions again to model how the state transitions:
      def loadKeys: IndexedStateT[Task, Empty.type, ProcessingKeys, Unit] =
        IndexedStateT.setF {
          Task(ProcessingKeys(List.fill(10)(Random.nextString(5))))
        }

      def pruneSomeKeys: StateT[Task, ProcessingKeys, Unit] =
        StateT.modifyF { s =>
          Task(s.copy(keys = s.keys take 3))
        }

      def pruneMoreKeys: StateT[Task, ProcessingKeys, Unit] =
        StateT.modifyF { s =>
          Task(s.copy(keys = s.keys drop 1))
        }

      def createDF(implicit spark: SparkSession): IndexedStateT[Task, ProcessingKeys, ProcessingDF, Unit] = {
        import spark.implicits._

        IndexedStateT.modifyF { s =>
          Task(ProcessingDF(s.keys.toDF))
        }
      }

      def transformDF(implicit spark: SparkSession): IndexedStateT[Task, ProcessingDF, Done, Unit] =
        IndexedStateT.modifyF { s =>
          Task(Done(s.df limit 3))
        }

      // Note how functions that stay within the same state type are still using the plain StateT.
      // This is because StateT is actually an alias for IndexedStateT[F, S, S, A] - a state transition that does not change the state type.

      def buildSession: Task[SparkSession] = Task {
        SparkSession
          .builder
          .appName("test")
          .master("local")
          .config("spark.scheduler.mode", "FAIR")
          .getOrCreate
      }

      // We can now launch our program with an accurate empty, uninitialized state and get back the Done state:
      def program(implicit spark: SparkSession): IndexedStateT[Task, Empty.type, Done, Unit] = for {
        _ <- loadKeys
        _ <- pruneSomeKeys
        _ <- pruneMoreKeys
        _ <- createDF
        _ <- transformDF
      } yield ()

      val (s: Done, _) =
        buildSession.flatMap(spark => program(spark).run(Empty)).runSyncUnsafe()

      println(s)

      // If we want to combine the timing functionality from the previous section, that’s also entirely possible;
      // we’d need to define a monad stack of IndexedStateT[TimedTask, SA, SB, A] and define the timed combinators for this stack.
    }
  }
}*/