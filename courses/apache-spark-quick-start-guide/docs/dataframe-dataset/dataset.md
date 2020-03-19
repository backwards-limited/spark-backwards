# Dataset

Think of a Dataset as a more type safe version of DataFrame e.g. when reading a CSV into a Dataset (unlike DataFrame) we have to provide an ADT that data will be encoded to, and upon read we must stipulate **as[ADT]**.

Important differences in a dataset compared to DataFrames are as follows:

- Defining a case class to define types of columns in CSV
- If the interpreter takes up a different type by inference, we need to cast to the exact type using the withColumn property

e.g.

```scala
case class Sales(
	id: Int, firstname: String, lastname: String,
  address: String, city: String, state: String, zip: String,
  ip: String, product_id: String, dop: String
)
```

```scala
val salesDataset: Dataset[Sales] = spark
	.read
	.option("sep", "\t")
  .option("header", "true")
  .csv("data/input/sample_10000.txt")
  .withColumn("id", 'id.cast(IntegerType))
  .as[Sales]
```

It is possible that the data (in the CSV) does not match the type defined (in this case Sales).

There are three options to deal with this situation:

**PERMISSIVE**: This is the default mode in which, if the data type is not matched with the schema type, the data fields are replaced with null:

```scala
val salesDataset: Dataset[Sales] = spark
	.read
	.option("mode", "PERMISSIVE")	
	.option("sep", "\t")
  .option("header", "true")
  .csv("data/input/sample_10000.txt")
  .withColumn("id", 'id.cast(IntegerType))
  .as[Sales]
```

**DROPMALFORMED**: As the name suggests, this mode will drop records where the parser finds a mismatch between the data type and schema type:

```scala
val salesDataset: Dataset[Sales] = spark
	.read
	.option("mode", "DROPMALFORMED")	
	.option("sep", "\t")
  .option("header", "true")
  .csv("data/input/sample_10000.txt")
  .withColumn("id", 'id.cast(IntegerType))
  .as[Sales]
```

**FAILFAST**: This mode will abort further processing on the first mismatch between data type and schema type:

```scala
val salesDataset: Dataset[Sales] = spark
	.read
	.option("mode", "FAILFAST")	
	.option("sep", "\t")
  .option("header", "true")
  .csv("data/input/sample_10000.txt")
  .withColumn("id", 'id.cast(IntegerType))
  .as[Sales]
```

Datasets work on the concept of *lazy evaluation* - we can **explain** to see the logical and optimised physical plan:

```scala
salesDataset.explain
```

```bash
== Physical Plan ==
*(1) Project [cast(id#10 as int) AS id#30, firstname#11, lastname#12, address#13, city#14, state#15, zip#16, ip#17, product_id#18, dop#19]
+- *(1) FileScan csv [id#10,firstname#11,lastname#12,address#13,city#14,state#15,zip#16,ip#17,product_id#18,dop#19] Batched: false, Format: CSV, Location: InMemoryFileIndex[file:/Users/davidainslie/workspace/backwards/spark-backwards/data/input/sample_..., PartitionFilters: [], PushedFilters: [], ReadSchema: struct<id:string,firstname:string,lastname:string,address:string,city:string,state:string,zip:str...
```

