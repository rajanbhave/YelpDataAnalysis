FROM openjdk:8-alpine
RUN apk --update add wget tar bash
RUN wget http://apache.mirror.anlx.net/spark/spark-2.3.3/spark-2.3.3-bin-hadoop2.7.tgz
RUN tar -xzf spark-2.3.3-bin-hadoop2.7.tgz && \
    mv spark-2.3.3-bin-hadoop2.7 /spark && \
    rm spark-2.3.3-bin-hadoop2.7.tgz

COPY start-master.sh /start-master.sh