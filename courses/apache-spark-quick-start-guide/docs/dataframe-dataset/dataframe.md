# DataFrame

DataFrames are distributed collections of data that are organized in the form of rows and columns.

- DataFrames can process data that's available in different formats, such as CSV, AVRO, and JSON, or stored in any storage media, such as Hive, HDFS, and RDBMS
- DataFrames can process data volumes from kilobytes to petabytes
- Use the Spark-SQL query optimizer to process data in a distributed and optimized manner

We can use a **Spark session object** to convert RDD into DataFrame of to load data directly from a file into DataFrame.

[DataFrameSpec.scala](../../src/test/scala/com/backwards/spark/dataframe/DataFrameSpec.scala) loads CSV from path within the context of this repository:

```scala
import org.apache.spark.sql.SparkSession

val spark: SparkSession = SparkSession
	.builder
  .appName("DataFrame example")
  .config("spark.master", "local")
  .config("spark.some.config.option", "value")
  .getOrCreate

val salesDataFrame: DataFrame = spark
	.read
  .option("sep", "\t")
  .option("header", "true")
  .csv("data/input/sample_10000.txt")

salesDataFrame.show
```

with output:

| id   | firstname | lastname | address | city | state | zip  | ip   | product_id | dop  |
| ---- | --------- | -------- | ------- | ---- | ----- | ---- | ---- | ---------- | ---- |
| 0    | Zena | Ross | 41228 West India Ln. | Powell | Tennessee | 21550 | 192.168.56.127 | PI_09 | 13/6/2014 |
| 1    | Elaine | Bishop | 15903 North North... | Hawaiian Gardens | Alaska | 06429 | 192.168.56.105 | PI_03 | 8/6/2014 |

Note **.csv** using a relative path where normally we might use **file://**, **hdfs://** or **s3://**.

**printSchema**

There is also (printing DataFrame in tree structure):

```scala
salesDataFrame.printSchema
```

```bash
root
 |-- id: string (nullable = true)
 |-- firstname: string (nullable = true)
 |-- lastname: string (nullable = true)
 |-- address: string (nullable = true)
 |-- city: string (nullable = true)
 |-- state: string (nullable = true)
 |-- zip: string (nullable = true)
 |-- ip: string (nullable = true)
 |-- product_id: string (nullable = true)
 |-- dop: string (nullable = true)
```

**select**

Select columns from a DataFrame:

```scala
salesDataFrame.select("firstname").show
```

| first name |
| ---------- |
| Zena       |
| Elaine     |
| Sage       |
| Cade       |
| Abra       |
| Stone      |
| Regina     |
| Donovan    |
| Aileen     |
| Mariam     |
| Silas      |
| Robin      |
| Galvin     |
| Alexa      |
| Tatyana    |
| Yuri       |
| Raya       |
| Ulysses    |
| Edward     |
| Emerald    |

only showing top 20 rows.

**filter**

```scala
salesDataFrame.filter($"id" < 5).show
```

| id   | firstname | lastname | address | city | state | zip  | ip   | product_id | do   |
| ---- | --------- | -------- | ------- | ---- | ----- | ---- | ---- | ---------- | ---- |
| 0    | Zena | Ross | 41228 West India Ln. | Powell | Tennessee | 21550 | 192.168.56.127 | PI_09 | 13/6/2014 |
| 1    | Elaine | Bishop | 15903 North North... | Hawaiian Gardens | Alaska | 06429 | 192.168.56.105 | PI_03 | 8/6/2014 |
| 2    | Sage | Carroll | 6880  Greenland Ct. | Guayanilla | Nevada | 08899 | 192.168.56.40 | PI_03 | 13/6/2014 |
| 3    | Cade | Singleton | 64021 South Bulga... | Derby | Missouri | 11233 | 192.168.56.171 | PI_06 | 14/6/2014 |
| 4    | Abra | Wright | 50155 South Mongo... | Port Jervis | New Jersey | 17751 | 192.168.56.52 | PI_09 | 11/6/2014 |

**groupBy**

Group rows in DataFrame based on set of columns and apply aggregated functions such as **count()**, **avg()**:

```scala
  salesDataFrame.groupBy("ip").count().show
```

| ip   | count |
| ---- | ----- |
| 192.168.56.141 | 35   |
| 192.168.56.30 | 41   |
| 192.168.56.129 | 36   |
| 192.168.56.91 | 39   |
| 192.168.56.36 | 40   |
| 192.168.56.170 | 46   |
| 192.168.56.173 | 44   |
| 192.168.56.54 | 45   |
| 192.168.56.100 | 32   |
| 192.168.56.70 | 35   |
| 192.168.56.190 | 39   |
| 192.168.56.7 | 33   |
| 192.168.56.119 | 40   |
| 192.168.56.34 | 29   |
| 192.168.56.56 | 39   |
| 192.168.56.57 | 49   |
| 192.168.56.104 | 42   |
| 192.168.56.15 | 41   |
| 192.168.56.13 | 36   |
| 192.168.56.216 | 47   |
only showing top 20 rows.

## Parquet

```scala
salesDataFrame.write.parquet("target/sales.parquet")

val parquetSalesDataFrame = spark
	.read
	.parquet("target/sales.parquet")

parquetSalesDataFrame.createOrReplaceTempView("parquetSales")

val ipDataFrame = spark
	.sql("SELECT ip FROM parquetSales WHERE id BETWEEN 10 AND 19")

ipDataFrame.map(row => s"IP: ${row(0)}").show
```

with output:

```bash
Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "id",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "firstname",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "lastname",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "address",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "city",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "state",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "zip",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "ip",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "product_id",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "dop",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary id (UTF8);
  optional binary firstname (UTF8);
  optional binary lastname (UTF8);
  optional binary address (UTF8);
  optional binary city (UTF8);
  optional binary state (UTF8);
  optional binary zip (UTF8);
  optional binary ip (UTF8);
  optional binary product_id (UTF8);
  optional binary dop (UTF8);
}
```

and output from **.show**:

| value              |
| ------------------ |
| IP: 192.168.56.253 |
| IP: 192.168.56.14  |
| IP: 192.168.56.198 |
| IP: 192.168.56.68  |
| IP: 192.168.56.95  |
| IP: 192.168.56.30  |
| IP: 192.168.56.102 |
| IP: 192.168.56.179 |
| IP: 192.168.56.247 |
| IP: 192.168.56.65  |

```bash
spark-backwards/target
➜ ls -las sales.parquet
total 696
  0 drwxr-xr-x  6 davidainslie  staff     192 16 Mar 20:50 .
  0 drwxr-xr-x  6 davidainslie  staff     192 16 Mar 20:50 ..
  8 -rw-r--r--  1 davidainslie  staff       8 16 Mar 20:50 ._SUCCESS.crc
  8 -rw-r--r--  1 davidainslie  staff    2708 16 Mar 20:50 .part-00000-74770592-32ef-4063-8655-57111fc2b81c-c000.snappy.parquet.crc
  0 -rw-r--r--  1 davidainslie  staff       0 16 Mar 20:50 _SUCCESS
680 -rw-r--r--  1 davidainslie  staff  345312 16 Mar 20:50 part-00000-74770592-32ef-4063-8655-57111fc2b81c-c000.snappy.parquet
```

## JSON

```scala
salesDataFrame.write.json("target/sales.json")

val jsonSalesDataFrame = spark.read.json("target/sales.json")

jsonSalesDataFrame.createOrReplaceTempView("jsonSales")

val ipDataFrame = spark
	.sql("SELECT ip FROM jsonSales WHERE id BETWEEN 10 AND 19")

ipDataFrame.map(row => s"IP:${row(0)}").show
```

Which outputs same IP table as parquet.

```bash
spark-backwards/target
➜ ls -las sales.json
total 4056
   0 drwxr-xr-x  6 davidainslie  staff      192 16 Mar 21:02 .
   0 drwxr-xr-x  7 davidainslie  staff      224 16 Mar 21:02 ..
   8 -rw-r--r--  1 davidainslie  staff        8 16 Mar 21:02 ._SUCCESS.crc
  32 -rw-r--r--  1 davidainslie  staff    16064 16 Mar 21:02 .part-00000-2db3c129-5c93-4e38-b31f-186e6227d786-c000.json.crc
   0 -rw-r--r--  1 davidainslie  staff        0 16 Mar 21:02 _SUCCESS
4016 -rw-r--r--  1 davidainslie  staff  2054721 16 Mar 21:02 part-00000-2db3c129-5c93-4e38-b31f-186e6227d786-c000.json
```

## Running SQL on DataFrames

DataFrames allow you to run SQL directly on data. For this, all we need to do is create temporary views on DataFrames. These views are categorized as local or global views. We saw this above in **parquet** and **JSON** which are examples of **temporary views that only last for the session**. If we want to have views available across various sessions, we need to create **Global Temporary Views**. The view definition is stored in the default database, **global_temp**:

```scala
salesDataFrame.createGlobalTempView("sales")

// Global temporary view is tied to a system database `global_temp`
spark.sql("SELECT * FROM global_temp.sales").show
spark.newSession().sql("SELECT * FROM global_temp.sales").show
```

