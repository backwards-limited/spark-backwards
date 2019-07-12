# Introduction

Spark runs on several types of clusters:

- Spark standalone cluster.
- Hadoop’s YARN (yet another resource negotiator).
- Mesos.

Spark consists of several purpose-built components:

- Spark Core
- Spark SQL
- Spark Streaming
- Spark GraphX
- Spark MLlib

## Spark Core

Spark Core contains basic Spark functionalities required for running jobs and needed by other components. The most important of these is the *resilient distributed dataset* (RDD), which is the main element of the Spark API. It’s an abstraction of a *distributed* collection of items with operations and transformations applicable to the dataset. It’s *resilient* because it’s capable of rebuilding datasets in case of node failures.

Spark Core contains logic for accessing various filesystems, such as HDFS, GlusterFS, Amazon S3, and so on.

## Spark SQL

Spark SQL provides functions for manipulating large sets of distributed, structured data using an SQL subset supported by Spark and Hive SQL (HiveQL). With DataFrames and DataSets, Spark SQL became one of the most important Spark components. Spark SQL can also be used for reading and writing data to and from various structured formats and data sources, such as JavaScript Object Notation (JSON) files, Parquet files (a popular file format that allows for storing a schema along with the data), relational databases, Hive, and others. 

## Spark Streaming
Spark Streaming is a framework for ingesting real-time streaming data from various sources. The supported streaming sources include HDFS, Kafka, Flume, Twitter, ZeroMQ, and custom ones. Spark Streaming operations recover from failure automatically, which is important for online data processing. Spark Streaming represents streaming data using discretized streams (DStreams), which periodically create RDDs containing the data that came in during the last time window.

## Spark MLlib
Spark MLlib is a library of machine-learning algorithms grown from the MLbase project at UC Berkeley. Supported algorithms include logistic regression, naïve Bayes classification, support vector machines (SVMs), decision trees, random forests, linear regression, and k-means clustering.

## Spark GraphX
Graphs are data structures comprising vertices and the edges connecting them. GraphX provides functions for building graphs, represented as graph RDDs: EdgeRDD and VertexRDD. GraphX contains implementations of the most important algorithms of graph theory, such as page rank, connected components, shortest paths, SVD++, and others.