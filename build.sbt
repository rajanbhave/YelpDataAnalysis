name := "YelpDataAnalysis"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.0"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "2.4.1"
