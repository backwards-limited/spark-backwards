FROM ubuntu:18.04

LABEL authors="dainslie@gmail.com"

RUN apt-get update

# This step will install java 8 on the image
RUN apt-get install software-properties-common -y \
  && apt-add-repository ppa:webupd8team/java -y \
  && apt-get install -y wget \
  && apt-get update -y \
  && add-apt-repository ppa:openjdk-r/ppa \
  && apt-get update \
  && apt-get install -y openjdk-8-jdk supervisor

ENV SPARK_VERSION 2.4.3
ENV HADOOP_VERSION 2.7

# download and extract Spark
RUN wget https://archive.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz \
  && tar -xzf spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz \
  && mv spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION /opt/spark

# Set spark home
ENV SPARK_HOME /opt/spark
ENV PATH $SPARK_HOME/bin:$PATH

# Adding conf files to all images. This will be used in supervisord for running spark master/slave
COPY master.conf /opt/conf/master.conf
COPY slave.conf /opt/conf/slave.conf
COPY history-server.conf /opt/conf/history-server.conf

# Adding configurations for history server
COPY spark-defaults.conf /opt/spark/conf/spark-defaults.conf
RUN  mkdir -p /opt/spark-events

# Expose port 8080 for spark UI
EXPOSE 4040 6066 7077 8080 18080 8081

# Default command: this is just an option
CMD ["/opt/spark/bin/spark-shell", "--master", "local[*]"]