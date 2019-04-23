package com.newyorker

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.log4j.LogManager
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object YelpDataAnalysis {
  val log = LogManager.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    log.info("Starting YelpDataAnalysis - Parsing configuration")
    val props = ConfigFactory.load();
    val envProps = props.getConfig(args(0));
    var yelpDatasetDir: Option[String] = None


    if (args(1) == null) {
      println("Please pass Yelp dataset directory as a argument")
      return
    } else {
      yelpDatasetDir = Some(args(1))
    }

    FileUtils.getFileList(yelpDatasetDir.get, ".json") match {
      case Some(files) => {
        runJob(files, envProps)
      }
      case None => {
        println("No json files found")
        return
      }
    }

  }

  /**
    * Runs a Spark Job to analyze the Yelp Dataset
    * @param files List of yelp dataset json files
    * @param envProps A properties Config to fetch the application properties
    * @return
    */

  def runJob(files: List[File], envProps: Config): Unit = {
    val spark = SparkSession.
      builder().
      master(envProps.getString("execution.mode")).
      appName("YelpDataAnalysis")
      .config("spark.cassandra.connection.host", envProps.getString("cassandra.connection.host"))
      .config("spark.cassandra.auth.username", envProps.getString("cassandra.auth.username"))
      .config("spark.cassandra.auth.password", envProps.getString("cassandra.auth.password"))
      .getOrCreate()

    val cassandraDataWriter = getCassandraWriter(spark, envProps.getString("cassandra.keyspace"))
    spark.sparkContext.setLogLevel("INFO")
    val usersDF = spark.read.json(FileUtils.getFile(files, "user.json"))
    val businessDF = spark.read.json(FileUtils.getFile(files, "business.json"))
    val reviewDF = spark.read.json(FileUtils.getFile(files, "review.json"))
    //val checkinDF = spark.read.json(FileUtils.getFile(files, "checkin.json"))
    //val photoDF = spark.read.json(FileUtils.getFile(files, "photo.json"))
    //val tipDF = spark.read.json(FileUtils.getFile(files, "tip.json"))

    usersDF.createOrReplaceTempView("users")
    businessDF.createOrReplaceTempView("business")
    reviewDF.createOrReplaceTempView("review")
    //checkinDF.createOrReplaceTempView("checkin")
    //photoDF.createOrReplaceTempView("photo")
    //tipDF.createOrReplaceTempView("tip")

    log.debug(s"users schema::::${usersDF.printSchema()}");
    log.debug(s"business schema::::${businessDF.printSchema()}");
    log.debug(s"review schema::::${reviewDF.printSchema()}");


    //START::===============================================================================
    //Average number of stars, by category for top 100 users\
    printMessage("For the top 100 users finding the average number of stars, by category")

    val userReviewSQL = "SELECT u.user_id,u.name, u.review_count,r.business_id,r.stars FROM (SELECT * " +
      "FROM users ORDER BY review_count DESC LIMIT 100) u " +
      "JOIN review r ON u.user_id=r.user_id "

    val userReviewDF = spark.sql(userReviewSQL)
    userReviewDF.createOrReplaceTempView("user_review")


    val businessExpDF = businessDF.withColumn("category", explode(
      when(col("categories").isNotNull, split(col("categories"),","))
        .otherwise(array(lit(null).cast("string")))
    ))

    businessExpDF.createOrReplaceTempView("business_exp")

    val userReviewBusinessDF = spark.sql("SELECT  u.business_id,u.name,u.stars,trim(b.category) as category " +
      " FROM user_review u " +
      " JOIN business_exp b " +
      " ON u.business_id = b.business_id ")

    userReviewBusinessDF.createOrReplaceTempView("user_review_business")

    val avgStarsPerUserByCategory = spark.sql("SELECT name,category, sum(stars) as avgstars " +
      " FROM user_review_business " +
      " GROUP BY name,category ")
    cassandraDataWriter.write(avgStarsPerUserByCategory, "top_users_avg_stars")
    //END::===============================================================================

    //START::==============================================================================
    //Top 10 oldest registered yelpers")
    printMessage("Finding the Top 10 oldest registered yelpers")
    val oldest10YelpersSQL = "SELECT user_id, name, yelping_since " +
      "FROM users WHERE user_id IS NOT NULL " +
      "ORDER BY (yelping_since) ASC LIMIT 10"


    val oldest10YelpersDF = spark.sql(oldest10YelpersSQL)
    cassandraDataWriter.write(oldest10YelpersDF, "oldest_10yelpers")
    //END::===============================================================================

    //START::===============================================================================
    printMessage("Finding the Top 10 best rated 4 and 5 star hotels in Toronto")
    //Top 10 best rated 4 and 5 star hotels in Toronto
    val top10BestRatedHotelsDF = spark.sql("SELECT business_id, name, state, city " +
      "FROM business LATERAL VIEW explode(split(categories, ',')) tab AS cat " +
      "WHERE trim(cat) = 'Hotels' " +
      "AND stars >= 4 " +
      "AND city ='Toronto'  " +
      "ORDER BY review_count DESC LIMIT 10")

    cassandraDataWriter.write(top10BestRatedHotelsDF, "top10_best_rated_hotels")

    //END::===============================================================================
  }

  /**
    * Prints the required message to log
    * @param msg Message String
    * @return
    */
  def printMessage(msg: String) = {
    log.info("=" * msg.length)
    log.info(msg)
    log.info("=" * msg.length)
  }

   /**
    * Creates a Cassandra Writer instance
    * @param msg Message String*
     *
    * @return
    */
  def getCassandraWriter(spark: SparkSession, keySpace: String): CassandraDataWriter = {
    new CassandraDataWriter(spark, keySpace)
  }
}
