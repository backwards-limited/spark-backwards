# AWS S3, Glue and Athena

![S3 Glue Athena](images/s3-glue-athena.jpg)

## Glue

**Glue** allows you to run a Spark job as a **serverless** process.
With Glue, we can crawl a datastore and create a data catalog.
When Glue accesses S3, it is best (most secure) to go through a private VPC endpoint instead of exposing S3 publically.

![Glue access](images/glue-accesses-s3.jpg)

Go to VPC to create an endpoint:

![Endpoint](images/create-endpoint.jpg)

![Endpoint](images/endpoint.jpg)

Now let's add a Glue crawler (calling it "my-crawler"):

![Crawler](images/start-add-crawler.jpg)

![Crawler](images/crawler-defaults.jpg)

When making a **connection** (between Glue and S3) we will use the **endpoint** we created:

![Crawler](images/connection.jpg)

![Crawler](images/add-datastore.jpg)

We need to give permission to Glue to access S3 - via IAM:

![Crawler](images/glue-iam.jpg)

And "run on demand".
Finally, the crawler needs a database to store the data catalog - we create a new database with defaults:

![Crawler](images/crawler-output.jpg)

Run the crawler.
When done go to **Databases** and click on "my-database" and then click on "Tables in my-database":

![Crawler](images/my-database.jpg)

Take a look at the generated table which shows the catalog generated from the data in S3:

![Crawler](images/my-table.jpg)

## Athena

Athena can be used to query data in a datastore such as S3.
Upon querying S3, we query by attributes maybe being equal to something, but of course we need to know (well Athena needs to know) the attributes and their types.
This has already been handled for us via Glue, i.e. behind the scene, Athena uses the Glue catalog (that Glue itself generated) to query the data in S3.

Within Glue, we can "view data" where we will be taken to Athena:

![Glue view data](images/view-data.jpg)

![Athena](images/athena.jpg)

## Run Spark Transformation Job on AWS Glue

This time we'll run Spark on Glue to transform our data into another S3 bucket:

![Spark on Glue](images/spark-on-glue.jpg)

We first need a bucket where the transformed data will end up.
I'll name it **david-ainslie-transformed** and then create a new Glue crawler for this new bucket, naming it "my-tranformed-crawler" (reusing connection and database):

![Transformed crawler](images/my-transformed.jpg)

We need a Glue ETL job to perform the data tranformation. We add a job:

![ETL](images/add-job.jpg)

But first we need our associated IAM role to read/write to S3 by attaching the relevant policies:

![IAM](images/editing-iam.jpg)

![IAM](images/attach-policies.jpg)

![IAM](images/full-access.jpg)

And our new Spark job looks like:

![Spark](images/job-1.jpg)

![Spark](images/job-2.jpg)

![Spark](images/job-data-target.jpg)

Finally, we can run the job:

![Spark](images/job-running.jpg)

When the job completes, you can run the Glue crawler for the transformed bucket.