# Docker

The following is taken from the post [A Journey into Big Data with Apache Spark Part1](https://towardsdatascience.com/a-journey-into-big-data-with-apache-spark-part-1-5dfcc2bccdd2) and [Part2](https://towardsdatascience.com/a-journey-into-big-data-with-apache-spark-part-2-4511aa19a900).

Within with [root folder of this module](../):

```bash
$ docker build -t davidainslie/spark .
```

Run it:

```bash
$ docker run -it --rm davidainslie/spark /bin/sh
```

Inside the running docker instance execute:

```bash
/ # spark-class org.apache.spark.deploy.master.Master --ip `hostname` --port 7077 --webui-port 8080
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
19/04/27 21:16:41 INFO Master: Started daemon with process name: 7@4f81a149e993
...
```

Stop the Master and drop out of the container by using **CTRL+C** and then **CTRL+D**.

We have now successfully started running a Spark Master.

The next step is to get some Workers added to the cluster, but first, we need to set some configuration on the Master so that the Worker can talk to it. To make things easy, we’ll give the Master a proper name and expose the Master port (the `--port` option in the last command) and also make the WebUI available to us.

In order to do this, we tweak the `docker run` command to add the `--name`, `--hostname` and `-p`options:

```bash
$ docker run --rm -it --name spark-master --hostname spark-master \
  -p 7077:7077 -p 8080:8080 davidainslie/spark /bin/sh
```

```bash
$ docker ps
CONTAINER ID   IMAGE      COMMAND     PORTS                                    NAMES
f2a497bf8ad1   .../spark  "/bin/sh"   0.0.0.0:7077->7077, 0.0.0.0:8080->8080   spark-master
```

Once again inside the container run:

```bash
/ # spark-class org.apache.spark.deploy.master.Master --ip `hostname` --port 7077 --webui-port 8080
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
19/04/27 21:26:33 INFO Master: Started daemon with process name: 8@spark-master
19/04/27 21:26:33 INFO SignalUtils: Registered signal handler for TERM
19/04/27 21:26:33 INFO SignalUtils: Registered signal handler for HUP
19/04/27 21:26:33 INFO SignalUtils: Registered signal handler for INT
19/04/27 21:26:33 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
19/04/27 21:26:34 INFO SecurityManager: Changing view acls to: root
19/04/27 21:26:34 INFO SecurityManager: Changing modify acls to: root
19/04/27 21:26:34 INFO SecurityManager: Changing view acls groups to: 
19/04/27 21:26:34 INFO SecurityManager: Changing modify acls groups to: 
19/04/27 21:26:34 INFO SecurityManager: SecurityManager: authentication disabled; ui acls disabled; users  with view permissions: Set(root); groups with view permissions: Set(); users  with modify permissions: Set(root); groups with modify permissions: Set()
19/04/27 21:26:34 INFO Utils: Successfully started service 'sparkMaster' on port 7077.
19/04/27 21:26:34 INFO Master: Starting Spark master at spark://spark-master:7077
19/04/27 21:26:34 INFO Master: Running Spark version 2.4.2
19/04/27 21:26:35 INFO Utils: Successfully started service 'MasterUI' on port 8080.
19/04/27 21:26:35 INFO MasterWebUI: Bound MasterWebUI to 0.0.0.0, and started at http://spark-master:8080
19/04/27 21:26:35 INFO Master: I have been elected leader! New state: ALIVE
```

And navigate to [http://localhost:8080](http://localhost:8080):

![Master](images/master.png)

As we are using Docker for Mac, DNS is painful and accessing the container by IP nigh on impossible. Luckily, Docker has its own networking capability (the specifics of which are out of scope of this post, too) which we’ll use to create a network for the local cluster to sit within. Creating a network is pretty simple and is done by running the following command:

```bash
$ docker network create spark-network
1715dab7a10090f2902c142e2a0b2dbf4d0fcc4988ea07cf79939867a2851d59
```

Stop the docker instance:

```bash
$ docker rm -f spark-master
```

To recreate the Master on the new network we can simply add the `--network` option to `docker run`:

```bash
$ docker run --rm -it --name spark-master --hostname spark-master \
  -p 7077:7077 -p 8080:8080 --network spark-network \
  davidainslie/spark /bin/sh
```

This is really no different to the first time we ran the Spark Master, except it uses a newly defined network that we can use to attach Workers to, to make the cluster work.

```bash
/ # spark-class org.apache.spark.deploy.master.Master --ip `hostname` --port 7077 --webui-port 8080
```

Now that the Master is up and running, let’s add a Worker node to it. This is where the magic of Docker really shines through.

To create a Worker and add it to the cluster, we can simply *launch a new instance of the same docker image* and run the command to start the Worker. We’ll need to give the Worker a new name:

```bash
$ docker run --rm -it --name spark-worker --hostname spark-worker \
  --network spark-network \
  davidainslie/spark /bin/sh
```

Within this new (worker) container run:

```bash
/ # spark-class org.apache.spark.deploy.worker.Worker \
  --webui-port 8080 spark://spark-master:7077
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
19/04/27 21:50:46 INFO Worker: Started daemon with process name: 8@spark-worker
...
19/04/27 21:50:47 INFO TransportClientFactory: Successfully created connection to spark-master/172.25.0.2:7077 after 101 ms (0 ms spent in bootstraps)
19/04/27 21:50:48 INFO Worker: Successfully registered with master spark://spark-master:7077
```

On the master we should see a log similar to:

```bash
19/04/27 21:50:48 INFO Master: Registering worker 172.25.0.3:40517 with 2 cores, 2.9 GB RAM
```

Now the UI should also show the new worker:

![Worker](images/worker.png)

To be a true test, we need to actually run some Spark code across the cluster. Let’s run a new instance of the docker image so we can run one of the examples provided when we installed Spark. Again, we can reuse the existing docker image and simply launch a new instance to use as the *driver* (the thing that submits the application to the cluster):

```bash
$ docker run --rm -it --network spark-network \
  davidainslie/spark /bin/sh
```

In the container, we can then submit an application to the cluster by running the following command:

```bash
/ # spark-submit --master spark://spark-master:7077 --class \
  org.apache.spark.examples.SparkPi \
  /spark/examples/jars/spark-examples_2.12-2.4.2.jar 1000
  
...
19/04/27 22:03:42 INFO DAGScheduler: ResultStage 0 (reduce at SparkPi.scala:38) finished in 65.504 s
19/04/27 22:03:42 INFO TaskSchedulerImpl: Removed TaskSet 0.0, whose tasks have all completed, from pool 
19/04/27 22:03:43 INFO DAGScheduler: Job 0 finished: reduce at SparkPi.scala:38, took 66.219067 s
Pi is roughly 3.1418093514180936
...
```

We'll be able to see the running job in the UI:

![Running job](images/running-job.png)

And once it is complete:

![Completed job](images/completed-job.png)

## Docker Compose

There are some (helper) scripts: [start-master.sh](../start-master.sh) and [start-worker.sh](../start-worker.sh).

(With everything shutdown) Rebuild and publish:

```bash
$ docker build -t davidainslie/spark .
```

```bash
$ docker push davidainslie/spark
```

And now for the [docker-compose](../docker-compose.yml) file:

```yaml
version: "3.7"

services:
  spark-master:
    image: davidainslie/spark
    container_name: spark-master
    hostname: spark-master
    ports:
      - 8080:8080
      - 7077:7077
    networks:
      - spark-network
    environment:
      SPARK_LOCAL_IP: spark-master
      SPARK_MASTER_PORT: 7077
      SPARK_MASTER_WEBUI_PORT: 8080
    command: "/start-master.sh"

  spark-worker:
    image: davidainslie/spark
    depends_on:
      - spark-master
    ports:
      - 8080
    networks:
      - spark-network
    environment:
      SPARK_MASTER: spark://spark-master:7077
      SPARK_WORKER_WEBUI_PORT: 8080
    command: "/start-worker.sh"
    volumes:
      - "./:/local"

networks:
  spark-network:
    driver: bridge
    ipam:
      driver: default
```

Run it:

```bash
$ docker-compose up
```

Finally we can run the **SparkPi** test again:

```bash
$ docker run --rm -it --network spark-and-hadoop-course_spark-network \
  davidainslie/spark /bin/sh
  
/ # spark-submit --master spark://spark-master:7077 --class \
  org.apache.spark.examples.SparkPi \
  /spark/examples/jars/spark-examples_2.12-2.4.2.jar 10  
```

To bring the cluster up, we simply run `docker-compose up`. One of the great things about Docker Compose is that we can scale the Workers by simply adding a `--scale` option to the compose command. Say we want 3 Worker nodes, we run:

```bash
$ docker-compose up --scale spark-worker=3
```

## Test Application

Let's submit a simple spark job. First make sure our spark containers are up and running (we can start with two workers) by executing our [docker-compose](../docker-compose.yml) file that is within this [module's directory](../):

```bash
$ docker-compose up --scale spark-worker=2
Creating network "spark-and-hadoop-course_spark-network" with driver "bridge"
Creating spark-master ... done
Creating docker_spark-worker_1 ... done
Creating docker_spark-worker_2 ... done
Attaching to spark-master, docker_spark-worker_2, docker_spark-worker_1
...
```

We shall use the following [SimepleExampleJob.scala](../src/main/scala/com/backwards/spark/SimpleExampleJob):

```scala
object SimpleExampleJob extends App {
  val SPARK_HOME = sys.env("SPARK_HOME")
  val logFile = s"$SPARK_HOME/README.md"

  val spark = SparkSession.builder
    .appName("first-scala-spark")
    .getOrCreate()

  val logData = spark.read.textFile(logFile).cache()
  val numAs = logData.filter(line => line.contains("a")).count()
  val numBs = logData.filter(line => line.contains("b")).count()
  println(s"Lines with a: $numAs, Lines with b: $numBs")

  spark.stop()
}
```

**Note** environment variables are set within our Spark docker image.

To submit this job we use the **spark-submit** utility, which is available in our Spark docker image - when we run and instantiate the image this time, we need to mount a volume to the current working directory of this [module](../) in order to access required resources within the container:

```bash
$ docker run --rm -it -e SPARK_MASTER="spark://spark-master:7077" \
  -v `pwd`:/project -v `pwd`:/local \
  --network spark-and-hadoop-course_spark-network \
  -w /project \
  davidainslie/spark /bin/bash
```

```bash
bash-4.4# spark-submit --master $SPARK_MASTER \
  --class com.backwards.spark.SimpleExampleJob \
  /project/target/scala-2.12/spark-and-hadoop-course_2.12-0.1.0-SNAPSHOT.jar

19/04/29 20:30:00 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
19/04/29 20:30:01 INFO SparkContext: Running Spark version 2.4.2
19/04/29 20:30:01 INFO SparkContext: Submitted application: first-scala-spark
...
19/04/29 20:30:19 INFO SparkContext: Starting job: count at SimpleExampleJob.scala:14
19/04/29 20:30:19 INFO DAGScheduler: Registering RDD 7 (count at SimpleExampleJob.scala:14)
19/04/29 20:30:19 INFO DAGScheduler: Got job 0 (count at SimpleExampleJob.scala:14) with 1 output partitions
19/04/29 20:30:19 INFO DAGScheduler: Final stage: ResultStage 1 (count at SimpleExampleJob.scala:14)
...
19/04/29 20:30:25 INFO DAGScheduler: Job 1 finished: count at SimpleExampleJob.scala:15, took 0.842583 s
Lines with a: 62, Lines with b: 31
```

![Job complete](images/job-complete.png)