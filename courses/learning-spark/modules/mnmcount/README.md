# MnMCount Module

## Build the package

```bash
sbt clean package
```

## Run M&M

```bash
spark-submit --class com.backwards.myspark.MnMcount target/scala-2.12/mnmcount_2.12-0.1.0-SNAPSHOT.jar data/mnm_dataset.csv
```
