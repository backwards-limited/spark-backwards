# Installation

![Installation](images/installation.png)

And wherever you have installed, add the following into your profile such as **.zshrc**:

```shell
export SPARK_HOME=/Applications/spark-3.0.0-preview2-bin-hadoop2.7
export PATH=${SPARK_HOME}/bin:${SPARK_HOME}/sbin:${PATH}
```

At the time of writing, Spark required Java 8 and if using Scala (as we shall be) then Scala 2.12. If using [jenv](https://www.jenv.be/) we can set the Java version locally:

```bash
/Applications/spark-3.0.0-preview2-bin-hadoop2.7
➜ jenv local 1.8.0.202
```

We can run a Python shell for Spark:

```
➜ pyspark
Python 3.7.6 (default, Dec 30 2019, 19:38:26)
...
SparkSession available as 'spark'.
>>>
```

And for Scala, including example interaction:

```bash
➜ spark-shell
...
Using Scala version 2.12.10 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_202)
Type in expressions to have them evaluated.
Type :help for more information.

scala> val strings = spark.read.text("README.md")
strings: org.apache.spark.sql.DataFrame = [value: string]

scala> strings.show(numRows = 10, truncate = false)
+--------------------------------------------------------------------------------+
|value                                                                           |
+--------------------------------------------------------------------------------+
|# Apache Spark                                                                  |
|                                                                                |
|Spark is a unified analytics engine for large-scale data processing. It provides|
|high-level APIs in Scala, Java, Python, and R, and an optimized engine that     |
|supports general computation graphs for data analysis. It also supports a       |
|rich set of higher-level tools including Spark SQL for SQL and DataFrames,      |
|MLlib for machine learning, GraphX for graph processing,                        |
|and Structured Streaming for stream processing.                                 |
|                                                                                |
|<https://spark.apache.org/>                                                     |
+--------------------------------------------------------------------------------+
only showing top 10 rows

scala> strings.count()
res1: Long = 109

scala>
```

Spark computations are expressed as operations. These operations are then converted into low-level RDD-based bytecode as tasks, which are then distributed to Spark’s workers for execution.

