FROM ubuntu:18.04

ENV SPARK_VERSION 2.4.3
ENV HADOOP_VERSION 2.7

# Run the following commands on my Linux machine install the below packages on the ubuntu image
RUN apt-get update -qq && \
    apt-get install -qq -y gnupg2 wget openjdk-8-jdk supervisor scala

# Download the Spark binaries from the repo
WORKDIR /

RUN wget --no-verbose http://www.gtlib.gatech.edu/pub/apache/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

# Untar the downloaded binaries , move them the folder name spark and add the spark bin on my class path
RUN tar -xzf /spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz && \
    mv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/spark && \
    echo "export PATH=$PATH:/opt/spark/bin" >> ~/.bashrc

# Adding conf files to all images. This will be used in supervisord for running spark master/slave
COPY master.conf /opt/conf/master.conf
COPY slave.conf /opt/conf/slave.conf
COPY history-server.conf /opt/conf/history-server.conf

# Adding configurations for history server
COPY spark-defaults.conf /opt/spark/conf/spark-defaults.conf
RUN  mkdir -p /opt/spark-events

# Expose the UI Port 4040
EXPOSE 4040 6066 7077 8080 18080 8081

# Default command: this is just an option
CMD ["/opt/spark/bin/spark-shell", "--master", "local[*]"]