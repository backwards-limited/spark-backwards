FROM ubuntu:18.04

LABEL authors="dainslie@gmail.com"

RUN apt-get -y update \
  && apt-get -y upgrade \
  && apt-get install -y wget \
  && apt-get install -y software-properties-common

RUN apt-get install -y openssh-server apache2 supervisor
RUN mkdir -p /var/lock/apache2 /var/run/apache2 /var/run/sshd /var/log/supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

RUN apt-get install -y openjdk-8-jdk

ENV SPARK_VERSION 2.4.1
ENV HADOOP_VERSION 2.7

# Download and extract Spark
RUN wget https://archive.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz \
  &&  tar -xzf spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION.tgz \
  &&  mv spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION /opt/spark

# Set spark home
ENV SPARK_HOME /opt/spark
ENV PATH $SPARK_HOME/bin:$PATH

# Adding conf files to all images. This will be used in supervisord for running spark master/slave
COPY master.conf /opt/conf/master.conf
COPY slave.conf /opt/conf/slave.conf
COPY history-server.conf /opt/conf/history-server.conf

COPY log4j.properties /opt/conf/log4j.properties
COPY log4j.properties /opt/spark/conf/log4j.properties

# Adding configurations for history server
COPY spark-defaults.conf /opt/spark/conf/spark-defaults.conf
RUN  mkdir -p /opt/spark-events

# Expose port 8080 for spark UI
EXPOSE 4040 6066 7077 8080 18080 8081

# Default command: this is just an option
CMD ["/opt/spark/bin/spark-shell", "--master", "local[*]"]