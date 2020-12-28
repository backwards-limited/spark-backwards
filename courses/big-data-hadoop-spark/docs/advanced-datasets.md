# Advanced Datasets

## User Defined Functions (UDF)

Following along on Colab (or Databricks) with the file [pyspark-udf-and-join.ipynb](../src/main/python/pyspark-udf-and-join.ipynb).

After loading CSVs [store-customers.csv](../src/main/resources/store-customers.csv) and [store-transactions.csv](../src/main/resources/store-transactions.csv):

```python
customerDF = spark.read.csv("store-customers.csv", header = True)

customerDF.show()

+----------+---+------+------+-------+
|CustomerID|Age|Salary|Gender|Country|
+----------+---+------+------+-------+
|         1| 72| 20000|  Male|Germany|
|         2| 72| 22000|Female| France|
|         3| 70| 24000|Female|England|
|         4| 75|  2600|  Male|England|
|         5| 33| 50000|  Male| France|
|         6| 52| 35000|Female|England|
|         7| 31|  4300|  Male|Germany|
|         8| 37| 32000|Female| France|
|         9| 76| 35000|  Male|Germany|
|        10| 58| 37000|Female| France|
|        11| 70| 25000|  Male|Germany|
|        12| 28| 27000|Female| France|
|        13| 21| 29000|Female|England|
|        14| 34|  7600|  Male|England|
|        15| 45| 55000|  Male| France|
|        16| 32| 40000|Female|England|
|        17| 62|  9300|  Male|Germany|
|        18| 54| 37000|Female| France|
|        19| 33| 40000|  Male|Germany|
|        20| 46| 42000|Female| France|
+----------+---+------+------+-------+
only showing top 20 rows

transactionsDF = spark.read.csv("store-transactions.csv", header = True)

transactionsDF.show()

+----------+---------+------+----------+
|CustomerID|ProductID|Amount|      Date|
+----------+---------+------+----------+
|      3427|        3|  7541|22-11-2019|
|      4378|       14|  7271|15-12-2019|
|      3751|       47|  4276|20-11-2019|
|      6899|      146|  8923|22-11-2019|
|      4561|       46|  4891|30-11-2019|
|      2299|      143|  7545|05-12-2019|
|       553|       43|  1147|08-12-2019|
|      3406|      134|   245|12-12-2019|
|      5278|       34|  8765|09-12-2019|
|      2456|       68|  3820|30-11-2019|
|      6963|       58|  1850|27-11-2019|
|      4700|      140|  3948|27-11-2019|
|      3566|       76|   401|17-12-2019|
|      2212|       38|  4750|16-12-2019|
|      4677|       18|  1149|18-11-2019|
|      4295|       35|  3241|19-11-2019|
|      4228|       91|  6405|01-12-2019|
|      2466|       83|  2978|10-12-2019|
|      3136|       67|  6581|28-11-2019|
|      6563|       80|  2878|21-11-2019|
+----------+---------+------+----------+
only showing top 20 rows
```

to extract year:

```python
from pyspark.sql.functions import udf

extract_year = udf(lambda date: date.split('-')[2]) # date parameter is a String

transactionsDF = transactionsDF.withColumn("year", extract_year(transactionsDF.Date))

transactionsDF.show()

+----------+---------+------+----------+----+
|CustomerID|ProductID|Amount|      Date|year|
+----------+---------+------+----------+----+
|      3427|        3|  7541|22-11-2019|2019|
|      4378|       14|  7271|15-12-2019|2019|
|      3751|       47|  4276|20-11-2019|2019|
|      6899|      146|  8923|22-11-2019|2019|
|      4561|       46|  4891|30-11-2019|2019|
|      2299|      143|  7545|05-12-2019|2019|
|       553|       43|  1147|08-12-2019|2019|
|      3406|      134|   245|12-12-2019|2019|
|      5278|       34|  8765|09-12-2019|2019|
|      2456|       68|  3820|30-11-2019|2019|
|      6963|       58|  1850|27-11-2019|2019|
|      4700|      140|  3948|27-11-2019|2019|
|      3566|       76|   401|17-12-2019|2019|
|      2212|       38|  4750|16-12-2019|2019|
|      4677|       18|  1149|18-11-2019|2019|
|      4295|       35|  3241|19-11-2019|2019|
|      4228|       91|  6405|01-12-2019|2019|
|      2466|       83|  2978|10-12-2019|2019|
|      3136|       67|  6581|28-11-2019|2019|
|      6563|       80|  2878|21-11-2019|2019|
+----------+---------+------+----------+----+
only showing top 20 rows
```

## Join - Left, Right, Inner, Outer

Which country customers spending more - Default join inner

```python
country_spend_details = customerDF.join(transactionsDF, customerDF.CustomerID == transactionsDF.CustomerID)

country_spend_details.show()

+----------+---+------+------+-------+----------+---------+------+----------+----+
|CustomerID|Age|Salary|Gender|Country|CustomerID|ProductID|Amount|      Date|year|
+----------+---+------+------+-------+----------+---------+------+----------+----+
|      3427| 69| 14300|Female|England|      3427|        3|  7541|22-11-2019|2019|
|      4378| 78| 42000|  Male|Germany|      4378|       14|  7271|15-12-2019|2019|
|      3751| 57| 45000|Female|England|      3751|       47|  4276|20-11-2019|2019|
|      6899| 34| 12600|  Male|Germany|      6899|      146|  8923|22-11-2019|2019|
|      4561| 37| 50000|Female|Germany|      4561|       46|  4891|30-11-2019|2019|
|      2299| 50| 12600|Female| France|      2299|      143|  7545|05-12-2019|2019|
|       553| 29| 42000|  Male|Germany|       553|       43|  1147|08-12-2019|2019|
|      3406| 37| 35000|Female|England|      3406|      134|   245|12-12-2019|2019|
|      5278| 64| 42000|Female|England|      5278|       34|  8765|09-12-2019|2019|
|      2456| 77| 35000|Female|Germany|      2456|       68|  3820|30-11-2019|2019|
|      6963| 26| 29000|  Male|Germany|      6963|       58|  1850|27-11-2019|2019|
|      4700| 40| 60000|Female|Germany|      4700|      140|  3948|27-11-2019|2019|
|      3566| 56| 40000|  Male|England|      3566|       76|   401|17-12-2019|2019|
|      2212| 28| 19300|  Male|England|      2212|       38|  4750|16-12-2019|2019|
|      4677| 37| 14300|  Male| France|      4677|       18|  1149|18-11-2019|2019|
|      4295| 60| 42000|  Male|Germany|      4295|       35|  3241|19-11-2019|2019|
|      4228| 36| 42000|Female|England|      4228|       91|  6405|01-12-2019|2019|
|      2466| 75| 40000|  Male|England|      2466|       83|  2978|10-12-2019|2019|
|      3136| 39| 50000|Female| France|      3136|       67|  6581|28-11-2019|2019|
|      6563| 47| 29000|Female|England|      6563|       80|  2878|21-11-2019|2019|
+----------+---+------+------+-------+----------+---------+------+----------+----+
only showing top 20 rows

country_spend_details.groupBy("Country").agg({"Amount": "sum"}).show()

+-------+------------+
|Country| sum(Amount)|
+-------+------------+
|Germany|1.34377551E8|
| France|1.45868556E8|
|England|2.30855023E8|
+-------+------------+
```

To explore the other joins, we'll work with "mini" versions of the previous CSVs:

```python
customerMiniDF = spark.read.csv("store-customers-mini.csv", header = True)

transactionMiniDF = spark.read.csv("store-transactions-mini.csv", header = True)
```

Inner join i.e. default:

```python
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID == transactionMiniDF.CustomerID).show()

# Which is equivalent to:
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID == transactionMiniDF.CustomerID, how = "inner").show()

+----------+---+------+------+-------+----------+---------+------+----------+
|CustomerID|Age|Salary|Gender|Country|CustomerID|ProductID|Amount|      Date|
+----------+---+------+------+-------+----------+---------+------+----------+
|         1| 72| 20000|  Male|Germany|         1|        3|  7541|22-11-2019|
|         2| 72| 22000|Female| France|         2|       14|  7271|15-12-2019|
|         5| 33| 50000|  Male| France|         5|       46|  4891|30-11-2019|
|         6| 52| 35000|Female|England|         6|      143|  7545|05-12-2019|
+----------+---+------+------+-------+----------+---------+------+----------+
```

Left join:

```python
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID == transactionMiniDF.CustomerID, how = "left").show()

+----------+---+------+------+-------+----------+---------+------+----------+
|CustomerID|Age|Salary|Gender|Country|CustomerID|ProductID|Amount|      Date|
+----------+---+------+------+-------+----------+---------+------+----------+
|         1| 72| 20000|  Male|Germany|         1|        3|  7541|22-11-2019|
|         2| 72| 22000|Female| France|         2|       14|  7271|15-12-2019|
|         5| 33| 50000|  Male| France|         5|       46|  4891|30-11-2019|
|         6| 52| 35000|Female|England|         6|      143|  7545|05-12-2019|
|         7| 31|  4300|  Male|Germany|      null|     null|  null|      null|
|         8| 37| 32000|Female| France|      null|     null|  null|      null|
+----------+---+------+------+-------+----------+---------+------+----------+
```

Notice the "nulls" because there were no matching records on the right part of the join, i.e. we include all the left.

Right join:

```python
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID == transactionMiniDF.CustomerID, how = "right").show()

+----------+----+------+------+-------+----------+---------+------+----------+
|CustomerID| Age|Salary|Gender|Country|CustomerID|ProductID|Amount|      Date|
+----------+----+------+------+-------+----------+---------+------+----------+
|         1|  72| 20000|  Male|Germany|         1|        3|  7541|22-11-2019|
|         2|  72| 22000|Female| France|         2|       14|  7271|15-12-2019|
|      null|null|  null|  null|   null|         3|       47|  4276|20-11-2019|
|      null|null|  null|  null|   null|         4|      146|  8923|22-11-2019|
|         5|  33| 50000|  Male| France|         5|       46|  4891|30-11-2019|
|         6|  52| 35000|Female|England|         6|      143|  7545|05-12-2019|
+----------+----+------+------+-------+----------+---------+------+----------+
```

Notice the "nulls" because there were no matching records on the left part of the join, i.e. we include all the right.

Full outer join:

```python
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID == transactionMiniDF.CustomerID, how = "full").show()

+----------+----+------+------+-------+----------+---------+------+----------+
|CustomerID| Age|Salary|Gender|Country|CustomerID|ProductID|Amount|      Date|
+----------+----+------+------+-------+----------+---------+------+----------+
|         7|  31|  4300|  Male|Germany|      null|     null|  null|      null|
|      null|null|  null|  null|   null|         3|       47|  4276|20-11-2019|
|         8|  37| 32000|Female| France|      null|     null|  null|      null|
|         5|  33| 50000|  Male| France|         5|       46|  4891|30-11-2019|
|         6|  52| 35000|Female|England|         6|      143|  7545|05-12-2019|
|         1|  72| 20000|  Male|Germany|         1|        3|  7541|22-11-2019|
|      null|null|  null|  null|   null|         4|      146|  8923|22-11-2019|
|         2|  72| 22000|Female| France|         2|       14|  7271|15-12-2019|
+----------+----+------+------+-------+----------+---------+------+----------+
```

Left semi join - like an inner join with left data frame value displayed:

```python
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID == transactionMiniDF.CustomerID, how = "left_semi").show()

+----------+---+------+------+-------+
|CustomerID|Age|Salary|Gender|Country|
+----------+---+------+------+-------+
|         1| 72| 20000|  Male|Germany|
|         2| 72| 22000|Female| France|
|         5| 33| 50000|  Male| France|
|         6| 52| 35000|Female|England|
+----------+---+------+------+-------+
```

Left anti join - rows in the left dataframe that are not present in the right dataframe:

```python
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID == transactionMiniDF.CustomerID, how = "left_anti").show()

+----------+---+------+------+-------+
|CustomerID|Age|Salary|Gender|Country|
+----------+---+------+------+-------+
|         7| 31|  4300|  Male|Germany|
|         8| 37| 32000|Female| France|
+----------+---+------+------+-------+
```

Advanced inner join:

```python
customerMiniDF.join(transactionMiniDF, customerMiniDF.CustomerID > transactionMiniDF.CustomerID, how = "inner").show()

+----------+---+------+------+-------+----------+---------+------+----------+
|CustomerID|Age|Salary|Gender|Country|CustomerID|ProductID|Amount|      Date|
+----------+---+------+------+-------+----------+---------+------+----------+
|         2| 72| 22000|Female| France|         1|        3|  7541|22-11-2019|
|         5| 33| 50000|  Male| France|         1|        3|  7541|22-11-2019|
|         5| 33| 50000|  Male| France|         2|       14|  7271|15-12-2019|
|         5| 33| 50000|  Male| France|         3|       47|  4276|20-11-2019|
|         5| 33| 50000|  Male| France|         4|      146|  8923|22-11-2019|
|         6| 52| 35000|Female|England|         1|        3|  7541|22-11-2019|
|         6| 52| 35000|Female|England|         2|       14|  7271|15-12-2019|
|         6| 52| 35000|Female|England|         3|       47|  4276|20-11-2019|
|         6| 52| 35000|Female|England|         4|      146|  8923|22-11-2019|
|         6| 52| 35000|Female|England|         5|       46|  4891|30-11-2019|
|         7| 31|  4300|  Male|Germany|         1|        3|  7541|22-11-2019|
|         7| 31|  4300|  Male|Germany|         2|       14|  7271|15-12-2019|
|         7| 31|  4300|  Male|Germany|         3|       47|  4276|20-11-2019|
|         7| 31|  4300|  Male|Germany|         4|      146|  8923|22-11-2019|
|         7| 31|  4300|  Male|Germany|         5|       46|  4891|30-11-2019|
|         7| 31|  4300|  Male|Germany|         6|      143|  7545|05-12-2019|
|         8| 37| 32000|Female| France|         1|        3|  7541|22-11-2019|
|         8| 37| 32000|Female| France|         2|       14|  7271|15-12-2019|
|         8| 37| 32000|Female| France|         3|       47|  4276|20-11-2019|
|         8| 37| 32000|Female| France|         4|      146|  8923|22-11-2019|
+----------+---+------+------+-------+----------+---------+------+----------+
only showing top 20 rows
```