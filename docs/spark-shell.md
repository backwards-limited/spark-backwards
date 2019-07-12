# Spark Shell

There are two different ways you can interact with Spark. One way is to write a program in Scala (Java or Python) that uses Spark’s library - that is, its API. The other is to use the *Scala shell* (or *Python shell*).

```bash
spark@spark-in-action:~$ spark-shell
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel).
Spark context Web UI available at http://10.0.2.15:4040
Spark context available as 'sc' (master = local[*], app id = local-1562532334327).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.0.0
      /_/

Using Scala version 2.11.8 (OpenJDK 64-Bit Server VM, Java 1.8.0_72-internal)
Type in expressions to have them evaluated.
Type :help for more information.

scala>
```

Give it a whirl:

```scala
scala> val lines = sc.textFile("/usr/local/spark/LICENSE")
lines: org.apache.spark.rdd.RDD[String] = /usr/local/spark/LICENSE MapPartitionsRDD[1] at textFile at <console>:24

scala> val lineCount = lines.count
lineCount: Long = 299

scala> val bsdLines = lines.filter(_.contains("BSD"))
bsdLines: org.apache.spark.rdd.RDD[String] = MapPartitionsRDD[2] at filter at <console>:26

scala> bsdLines.count
res0: Long = 33
```

