# Architecture Introduction

Take a look at [FirstApp](../src/main/scala/com/backwards/spark/FirstApp.scala) which locally reads a CSV, does some transformations, and persists to a Postgres database:

![Local program](images/local-program.png)

In a real world Spark application we'll have a Spark cluster:

![Spark cluster](images/spark-cluster.png)

Each worker will work on one or more partitions of the complete dataset:

![Partitions](images/partitions.png)

and in a bit more detail:

![Tasks](images/tasks.png)

where the tasks are our code e.g. FirstApplication which, after transforming data, wrote to a database:

![Write to database](images/write-to-database.png)

And note, that the data will be distributed, instead of reading a file from a single (local) machine as we do in FirstApplication.

![Distributed data](images/distributed-data.png)

## Master/Worker Processes

![Spark cluster](images/spark-cluster.png)

The Driver process contains our code e.g. FirstApplication:

![Driver process](images/driver-process.png)

However, the Driver does not execute our code - the spawned workers execute our code.