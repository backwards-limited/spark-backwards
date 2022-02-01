package com.backwards.spark

import java.nio.charset.CodingErrorAction
import scala.io.{Codec, Source}
import scala.util.{Try, Using}
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._
import better.files.Resource.{getUrl => resourceUrl}

object ALSMovieRecs {
  // Our Rating class
  case class Rating(userId: Int, movieId: Int, rating: Float)

  // Parse a line of movies.dat to a Rating
  def parseRating(str: String): Rating = {
    val fields = str.split("::")
    assert(fields.size == 4)
    Rating(fields(0).toInt, fields(1).toInt, fields(2).toFloat)
  }

  // Load up a Map of movie IDs to movie names.
  // This is "small data" so we just load it from disk into memory.
  def loadMovieNames: Map[Int, String] = {
    // Handle character encoding issues:
    implicit val codec: Codec = Codec("UTF-8")

    codec.onMalformedInput(CodingErrorAction.REPLACE)
    codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

    // Create a Map of Ints to Strings, and populate it from movies.dat
    val movieNames: Try[Map[Int, String]] = Using(Source.fromFile(resourceUrl("ml-1m/movies.dat").getFile)) { source =>
      source.getLines().foldLeft(Map.empty[Int, String]) { case (m, line) =>
        val fields = line.split("::")

        if (fields.length > 1) {
          m + (fields(0).toInt -> fields(1))
        } else {
          m
        }
      }
    }

    movieNames.fold(throw _, identity)
  }

  def main(args: Array[String]): Unit = {
    // Set up our SparkSession
    val spark = SparkSession
      .builder()
      .appName("ALSExample")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Load map of movie ID's to movie names in memory
    println("Loading movie names...")
    val nameDict = loadMovieNames

    import spark.implicits._

    val hadoopConf = spark.sparkContext.hadoopConfiguration
    hadoopConf.set("fs.s3.impl", "org.apache.hadoop.fs.s3native.NativeS3FileSystem")

    // Change to your own S3 bucket - load the ratings "big data" to train our recommender model with.
    val ratings = spark.read.textFile("s3a://david-ainslie/ml-1m/ratings.dat").map(parseRating)

    // Count up the number of ratings for each movie for later use
    val ratingCounts = ratings.groupBy("movieId").count()

    // Create an Alternating Least Squares recommender with given parameters
    val als = new ALS()
      .setRank(8)
      .setMaxIter(10)
      .setRegParam(0.1)
      .setSeed(1234)
      .setUserCol("userId")
      .setItemCol("movieId")
      .setRatingCol("rating")

    // Fabricate a new user ID 0 who likes sci-fi and older classics, but hates The Rocky Horror Picture Show.
    val newUserRatings = Array(
      Rating(0,260,5), // Star Wars
      Rating(0,329,5), // Star Trek Generations
      Rating(0,1356,4), // Star Trek First Contact
      Rating(0,904,5), // Rear Window
      Rating(0,908,4), // North by Northwest
      Rating(0,2657,1) // Rocky Horror Picture Show
    )

    val newUserRatingsDS: Dataset[Rating] =
      spark.sparkContext.parallelize(newUserRatings.toIndexedSeq).toDS()

    // Add this new user into the ratings to train ALS with.
    val allRatings: Dataset[Rating] = ratings.union(newUserRatingsDS)

    // Train our ALS movie recommender model.
    val model: ALSModel = als.fit(allRatings)

    // Build a dataset of movies user ID 0 has not seen, which have been rated more than 25 times.
    val moviesIveSeen: Array[Int] = newUserRatings.map(_.movieId)

    val unratedMovies: Dataset[Rating] =
      ratings.filter(x => !(moviesIveSeen contains x.movieId))

    val myUnratedMovies: Dataset[Rating] =
      unratedMovies
        .map(x => Rating(0, x.movieId, 0))
        .distinct()

    val myUnratedMoviesWithCounts: DataFrame =
      myUnratedMovies.join(ratingCounts, "movieId")

    val myPopularUnratedMovies: Dataset[Row] =
      myUnratedMoviesWithCounts.filter(myUnratedMoviesWithCounts("count") > 25)

    // Predict ratings on each movie.
    val predictions: DataFrame = model.transform(myPopularUnratedMovies)

    // Print out the ratings of this user, together with movie titles
    println("\nRatings for user ID 0:")

    for (rating <- newUserRatings) {
      println(nameDict(rating.movieId) + ": " + rating.rating)
    }

    // Take the 10 movies with the highest rating predictions and print them out!
    println("\nTop 10 recommendations:")

    for (recommendation <- predictions.orderBy(desc("prediction")).take(10)) {
      println(nameDict(recommendation.getAs[Int]("movieId")) + " score " + recommendation.getAs[String]("prediction"))
    }

    // Stop the session when we're done.
    spark.stop()
  }
}