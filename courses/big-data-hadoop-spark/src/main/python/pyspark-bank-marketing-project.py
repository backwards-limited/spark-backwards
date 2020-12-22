from pyspark.sql import SparkSession
spark = SparkSession.builder.appName("spark-demo").getOrCreate()

"""# DataFrame

## Read the CSV file into a DataFrame
"""

bankProspectsDF = spark.read.csv("bank_prospects.csv",header=True)

"""## Remove the record with unknow value in country column"""

bankProspectsDF1 = bankProspectsDF.filter(bankProspectsDF['country'] != "unknown")

"""##  Cast the String datatype to Integer/Float"""

from pyspark.sql.types import IntegerType,FloatType

bankProspectsDF2 = bankProspectsDF1.withColumn("age", bankProspectsDF1["age"].cast(IntegerType())).withColumn("salary", bankProspectsDF1["salary"].cast(FloatType()))

"""## Replace Age and Salary with average values of their respective column

import mean from sql.fuctions
"""

from pyspark.sql.functions import mean

"""### Calculate "mean" value of the age"""

mean_age_val = bankProspectsDF2.select(mean(bankProspectsDF2['age'])).collect()

mean_age = mean_age_val[0][0]

"""### Calculate mean salary value"""

mean_salary_val = bankProspectsDF2.select(mean(bankProspectsDF2['salary'])).collect()

mean_salary = mean_salary_val[0][0]

"""### Replace missing age with average value"""

bankbankProspectsDF3 = bankProspectsDF2.na.fill(mean_age,["age"])

"""### Replace missing age with salary value"""

bankbankProspectsDF4 = bankbankProspectsDF3.na.fill(mean_salary,["salary"])

"""## Write the transformed file to a new csv file"""

bankbankProspectsDF4.write.format("csv").save("bank_prospects_transformed")