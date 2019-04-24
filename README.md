# YelpDataAnalysis

## Introduction

This project showcases the processing of Yelp dataset using Spark 2.3.0 and Scala 2.11.8 to gain different insights by running some queries. The output of these queries will be stored in tabular format in Cassandra tables.

The spark code will be compiled and bundled using sbt.

Docker image would be then created using the Docker configuration.

Then we need to spin up a spark container cluster of 4 nodes – one master and three worker nodes,
which will be used for submitting our jar and executing our queries.

This project has been developed by using Intellij as IDE.

## Cassandra Configuration –
We need to create the required keyspace and tables in java using the below Cassandra script.

This creates keyspace – ‘yelp_analysis_ks’

And below three tables –
oldest_10yelpers <br>
top_users_avg_stars<br>
top10_best_rated_hotels<br>

## Spark Job Details –
Spark uses typesafe application.properties file which will contain the required configuration.
We need to modify the Cassandra details in application.properties like the hostIP, keyspacename etc.

* In the spark job, we are performing three kinds of analysis –<br>
1.	For the top 100 users finding the average number of stars, by category
2.	Finding the Top 10 oldest registered yelpers
3.	Finding the Top 10 best rated 4 and 5 star hotels in Toronto <br>

* In spark, dataflow is as below –
1.	Spark job accepts yelp dataset tar as one of the input parameters.
2.	This tar is first uncompressed and individual files are captured
3.	These files are then read and converted into dataframes.
4.	These dataframes are then registered as temporary tables.
5.	Queries are run on these tables using Spark SQL.
6.	The output of these queries is again a dataframe which is further passed to Cassandra Data Writer.
7.	CassandraDataWriter writes these dataframes into respective Cassandra tables using the configurations provided in application.properties.

## Step by Step Project Execution Details - 
•	**Step** 1 Create a /project directory in your system

•	**Step** 2 Fetch the latest code from github using the below command in this directory –<br>
```git clone https://github.com/rajanbhave/YelpDataAnalysis```

•	**Step 3** cd into the project directory and run the below command to create a docker image of code.
```docker build -t rajan_bhave/yelp-analysis:latest --build-arg SCALA_VERSION=2.11.8 --build-arg SBT_VERSION=0.13.18 --build-arg SPARK_VERSION=2.3.3 --build-arg HADOOP_VERSION=2.7 -f ~/project/docker_configs/Dockerfile ~/project```

The above command will perform the following activities using the Docker file -
1.	Install Java JDK 8
2.	Spark 2.2.3
3.	Install Scala 2.11.8
4.	Install SBT 0.13.18
5.	Install Hadoop 2.7 Libraries
6.	Copy the code and required scripts to container image folders
7.	Change the working directory to our project directory

•	**Step 4** Once the image is created, spin up a spark container cluster using the below command –
```docker-compose up --scale spark-worker=3```

This command uses the docker-compose.yml as a reference and does the below activities –
1.	Creates a new container network ‘spark-network’
2.	Creates a new spark master container with the necessary configurations.
3.	Creates three new spark worker containers with the necessary configurations.

•	**Step 5** Once the cluster is created, we can confirm that by opening the spark web ui on http://<container_ip>:8080
We can see the master and three worker nodes as well as their details on that UI.

•	**Step 6** Now create another container using the below command to run our spark job –
```
docker run --rm -it -e SPARK_MASTER="spark://spark-master:7077" \
--network spark_network -w /project/yelp-analysis/ \
    rajan_bhave/yelp-analysis:latest /bin/bash
```

•	**Step 7** Once this container is created, run the below spark job which will run on the container spark cluster.
It will accept the yelp dataset tar as input file.
Edit the /project/application.properties and add the Cassandra host IP address.
This jar will perform the required analysis and store the results in Cassandra DB.
```
/spark/bin/spark-submit --packages com.typesafe:config:1.3.2,com.datastax.spark:spark-cassandra-connector_2.11:2.4.1 \
--repositories https://oss.sonatype.org/content/repositories/releases/ \
--master $SPARK_MASTER --deploy-mode client --conf spark.ui.port=22231 \
--files /project/yelp_dataset.tar \
--conf spark.driver.extraJavaOptions=-Dconfig.file=/project/application.properties \
--conf spark.executor.extraJavaOptions=-Dconfig.file=/project/application.properties \
--class com.newyorker.YelpDataAnalysis /project/target/scala-2.11/yelpdataanalysis_2.11-0.1.jar \
dev /project/yelp_dataset.tar
```
