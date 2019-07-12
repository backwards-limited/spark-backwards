FROM pavanpkulkarni/spark_image:2.2.1

COPY ./target/scala-2.12/spark-backwards.jar /opt/spark-backwards.jar

COPY ./data/input /opt