FROM openjdk:8-alpine

ARG SCALA_VERSION
ARG SBT_VERSION
ARG SPARK_VERSION
ARG HADOOP_VERSION

ENV SCALA_VERSION ${SCALA_VERSION:-2.11.8}
ENV SBT_VERSION ${SBT_VERSION:-0.13}
ENV SPARK_VERSION ${SPARK_VERSION:-2.3.3}
ENV HADOOP_VERSION ${HADOOP_VERSION:-2.7}

RUN apk --update add wget tar bash
RUN wget http://apache.mirror.anlx.net/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz
RUN tar -xzf spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz && \
    mv spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION /spark && \
    rm spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz

RUN \
  echo "$SCALA_VERSION $SBT_VERSION" && \
  mkdir -p /usr/lib/jvm/java-1.8-openjdk/jre && \
  touch /usr/lib/jvm/java-1.8-openjdk/jre/release && \
  apk add --no-cache bash && \
  apk add --no-cache curl && \
  curl -fsL http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /usr/local && \
  ln -s /usr/local/scala-$SCALA_VERSION/bin/* /usr/local/bin/ && \
  scala -version && \
  scalac -version

RUN \
  curl -fsL https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz | tar xfz - -C /usr/local && \
  ln -s /usr/local/sbt/bin/* /usr/local/bin/ && \
  sbt sbt-version || sbt sbtVersion || true

RUN mkdir /project
RUN chmod -R +x /project
ADD . /project

WORKDIR /project

CMD "/usr/local/bin/sbt"

RUN \
  sbt package
