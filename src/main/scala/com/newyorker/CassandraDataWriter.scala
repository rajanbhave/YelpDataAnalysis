package com.newyorker

import com.datastax.spark.connector._
import org.apache.log4j.LogManager
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

class CassandraDataWriter(spark: SparkSession, keySpace: String) {

  case class CassandraDataProperties(saveMode: SaveMode, keySpace: String)
  val log = LogManager.getLogger(this.getClass)
  val dbOptions = CassandraDataProperties(SaveMode.Overwrite, keySpace)

  /**
    * Writes a spark dataframe to Cassandra table
    * @param dataFrame spark dataframe
    * @param tableName Cassandra table name
    * @return
    */
  def write(dataFrame: DataFrame, tableName: String): Unit = {
    if (isCassandraConfigExist()) {
      //dataFrame.show(false)
      log.info(s"Writing Dataframe to Casandra' table ${tableName} in keyspace ${dbOptions.keySpace}")
      /*dataFrame.createCassandraTable(dbOptions.keySpace,tableName)
      dataFrame.write.cassandraFormat(tableName, dbOptions.keySpace).save()*/
      dataFrame.write
        .mode(dbOptions.saveMode)
        .format("org.apache.spark.sql.cassandra")
        .options(Map("table" -> tableName, "keyspace" -> dbOptions.keySpace, "confirm.truncate" -> "true"))
        .save()
    } else {
      log.error(s"Cassandra configurations were not provided")
    }
  }

  /**
    * Checks if the cassandra host name is specified in application config
    * @return
    */
  private def isCassandraConfigExist(): Boolean = spark.conf.getOption("spark.cassandra.connection.host").isDefined


}
