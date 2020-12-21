# Hadoop Commands

```bash
pwd

ls

wget https://raw.githubusercontent.com/futurexskill/bigdata/master/retailstore.csv

ls 

hadoop fs -ls

hadoop fs -mkdir /user/futurexskill

hadoop fs -mkdir /user/futurexskill/data

hadoop fs -ls

hadoop fs -put retailstore.csv /user/futurexskill/data

hadoop fs -ls /user/futurexskill/data


hadoop fs -cat /user/futurexskill/data/retailstore.csv

hadoop fs -rm /user/futurexskill/data/retailstore.csv

ls

hadoop fs -put retailstore.csv /user/futurexskill/data

hadoop fs -ls /user/futurexskill/data

hadoop fs -cat /user/futurexskill/data/retailstore.csv
===================================================


============================== from pyspark shell =========================================

customerDF = spark.read.csv("data/retailstore.csv",header=True)

or

customerDF = spark.read.csv("/user/futurexskill/data/retailstore.csv",header=True)

customerDF.show()

customerDF.groupBy("gender").count().show()
customerDF.createOrReplaceTempView("customer")
new_results = spark.sql("select * from customer where age>22").show()

===========================  using spark-submit ======================================
# Create a python file (dataframe_demo.py) using the following content


from pyspark.sql import SparkSession

spark = SparkSession.builder.appName('SparkDFDemo').getOrCreate()
customerDF = spark.read.csv("data/retailstore.csv",header=True)
customerDF.show()

customerDF.groupBy("gender").count().show()
customerDF.createOrReplaceTempView("customer")
new_results = spark.sql("select * from customer where age>22").show()



# run the file using "spark-submit dataframe_demo.py" command

=============================================================================
```