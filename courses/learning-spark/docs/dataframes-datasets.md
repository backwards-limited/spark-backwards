# DataFrames and Datasets

## DataFrame

To a human’s eye, a Spark DataFrame is like a table:

| **Id** **(Int)** | **First** **(String)** | **Last** **(String)** | **Url** **(String)** | **Published** **(Date)** | **Hits** **(Int)** | **Campaigns** **(List[Strings])** |
| ---------------- | ---------------------- | --------------------- | -------------------- | ------------------------ | ------------------ | --------------------------------- |
| 1                | Jules                  | Damji                 | https://tinyurl.1    | 1/4/2016                 | 4535               | [twitter, LinkedIn]               |
| 2                | Brooke                 | Wenig                 | https://tinyurl.2    | 5/5/2018                 | 8908               | [twitter, LinkedIn]               |
| 3                | Denny                  | Lee                   | https://tinyurl.3    | 6/7/2019                 | 7659               | [web, twitter, FB, LinkedIn]      |
| 4                | Tathagata              | Das                   | https://tinyurl.4    | 5/12/2018                | 10568              | [twitter, FB]                     |
| 5                | Matei                  | Zaharia               | https://tinyurl.5    | 5/14/2014                | 40578              | [web, twitter, FB, LinkedIn]      |
| 6                | Reynold                | Xin                   | https://tinyurl.6    | 3/2/2015                 | 25568              | [twitter, LinkedIn]               |

A *schema* in Spark defines the column names and its associated data types for a DataFrame. More often schemas come into play when you are reading structured data from DataSources (more on this in the next chapter). Defining schema as opposed to schema-on-read offers three benefits: 1) you relieve Spark from the onus of inferring data types 2) you prevent Spark from creating a separate job just to read a large portion of your file to ascertain the schema, which for a very large data file, can be expensive and time consuming, and 3) detects errors early if data doesn’t match the schema.

Spark allows you to define schema in two ways. One way is to define it programmatically. Another way is to employ a Data Definition Language (DDL) string.

For a programmatic example see [DataFrameSpec](../src/test/scala/com/backwards/spark/dataframe/DataFrameSpec.scala).