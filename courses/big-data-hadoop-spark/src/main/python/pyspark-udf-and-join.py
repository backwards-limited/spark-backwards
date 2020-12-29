# -*- coding: utf-8 -*-
"""PySpark_udf_and_join.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1o1jlcqs2JgPWBDPF6mRXB9LTXaZZ7spf

## Install JDK
## Install Spark
## Set Environment variables
## Create a Spark Session
"""

!apt-get install openjdk-8-jdk-headless -qq > /dev/null
!wget -q https://archive.apache.org/dist/spark/spark-2.4.3/spark-2.4.3-bin-hadoop2.6.tgz
!tar -xvf spark-2.4.3-bin-hadoop2.6.tgz
!pip install -q findspark
import os
os.environ["JAVA_HOME"] = "/usr/lib/jvm/java-8-openjdk-amd64"
os.environ["SPARK_HOME"] = "/content/spark-2.4.3-bin-hadoop2.6"
import findspark
findspark.init()
from pyspark.sql import SparkSession
spark = SparkSession.builder.master("local[*]").getOrCreate()

"""## Test Spark"""

df = spark.createDataFrame([{"Google": "Colab","Spark": "Scala"} ,{"Google": "Dataproc","Spark":"Python"}])
df.show()

"""## Copy the data files to your local Colab environment"""

!wget https://raw.githubusercontent.com/futurexskill/bigdata/master/store_customers.csv

!wget https://raw.githubusercontent.com/futurexskill/bigdata/master/store_transactions.csv

"""## Check if the file is copied"""

ls

"""# DataFrame

## Read the CSV file into a DataFrame
"""

customerDF = spark.read.csv("store_customers.csv",header=True)

customerDF.show()

customerDF.count()

transactionDF = spark.read.csv("store_transactions.csv",header=True)

transactionDF.show()

transactionDF.count()

"""## Extracting year using UDF"""

from pyspark.sql.functions import udf

extract_year = udf (lambda Date:Date.split('-')[2])

transactionDF = transactionDF.withColumn("year",extract_year(transactionDF.Date))

transactionDF.show()

"""## Join - Which country customers spending more

### Default join inner join
"""

country_spend_details = customerDF.join(transactionDF,customerDF.CustomerID == transactionDF.CustomerID )

country_spend_details.show()

country_spend_details.groupBy("Country").agg({"Amount" : "sum"}).show()

!wget https://raw.githubusercontent.com/futurexskill/bigdata/master/store_customers_mini.csv

!wget https://raw.githubusercontent.com/futurexskill/bigdata/master/store_transactions_mini.csv

!ls

customerDFMini = spark.read.csv("store_customers_mini.csv",header=True)

customerDFMini.show()

transactionDFMini = spark.read.csv("store_transactions_mini.csv",header=True)

transactionDFMini.show()

"""### Inner join"""

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID == transactionDFMini.CustomerID ).show()

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID == transactionDFMini.CustomerID,how="inner" ).show()

"""### Left join"""

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID == transactionDFMini.CustomerID, how="left" ).show()

"""### Right join"""

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID == transactionDFMini.CustomerID, how="right" ).show()

"""### full outer join"""

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID == transactionDFMini.CustomerID, how="full" ).show()

"""### left semi join
Like inner join with left data frame value displayed
"""

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID == transactionDFMini.CustomerID, how="left_semi" ).show()

"""### left anti join
Rows in left dataframe that are not present in right dataframe
"""

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID == transactionDFMini.CustomerID, how="left_anti" ).show()

"""### Inner join advanced"""

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID > transactionDFMini.CustomerID,how="inner" ).show()

customerDFMini.join(transactionDFMini,customerDFMini.CustomerID < transactionDFMini.CustomerID,how="inner" ).show()
