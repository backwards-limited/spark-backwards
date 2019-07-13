FROM davidainslie/spark

COPY ./target/scala-2.12/spark-backwards.jar /opt/spark-backwards.jar

COPY ./data/input /opt