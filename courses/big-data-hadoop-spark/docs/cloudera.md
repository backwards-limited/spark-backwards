# Cloudera

```bash
docker pull cloudera/quickstart:latest
```

```bash
docker run --hostname=quickstart.cloudera --privileged=true -it -p 7180:7180 -p 8888:8888 cloudera/quickstart /usr/bin/docker-quickstart
```

Port **7180** is for the Hadoop user interface, and port **8888** is for Cloudera manager.

Once up and running, go to [localhost:8888](http://localhost:8888) and login with the credentials:
- UserID: cloudera 
- Password: cloudera

We can interact with the hadoop file system e.g.

```bash
[root@quickstart /]# hadoop fs -mkdir abc

[root@quickstart /]# hadoop fs -ls
Found 1 items
drwxr-xr-x   - root supergroup          0 2020-12-30 12:06 abc
```

At this point we could start the **spark-shell**:

```bash
[root@quickstart /]# spark-shell
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/usr/lib/zookeeper/lib/slf4j-log4j12-1.7.5.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/usr/jars/slf4j-log4j12-1.7.5.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel).
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 1.6.0
      /_/

Using Scala version 2.10.5 (Java HotSpot(TM) 64-Bit Server VM, Java 1.7.0_67)
```

Oh dear. We have very old versions of:
- Spark version 1.6.0
- Scala version 2.10.5
- Java version 1.7

We are going to upgrade the running Docker container and save as a new image.

## Updated Docker Image

### Yum
Firstly, we may run into the following issue:

```bash
[root@quickstart /]# yum update
Loaded plugins: fastestmirror
Setting up Update Process
Loading mirror speeds from cached hostfile
YumRepo Error: All mirror URLs are not using ftp, http[s] or file.
 Eg. Invalid release/repo/arch combination/
removing mirrorlist with no valid mirrors: /var/cache/yum/x86_64/6/base/mirrorlist.txt
Error: Cannot find a valid baseurl for repo: base
```

We first resolve this issue by following [YumRepo Error](https://grepitout.com/yumrepo-error-all-mirror-urls-are-not-using-ftp-https-or-file/):

Make a backup of the existing CentOS Base repository file:

```bash
cp -pr /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.OLD
```

Edit the file /etc/yum.repos.d/CentOS-Base.repo using your favorite editor:

```bash
vim /etc/yum.repos.d/CentOS-Base.repo
```

Modify the mentioned file with the following content:

```ini
[base]
name=CentOS-$releasever - Base
# mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=os&infra=$infra
baseurl=http://vault.centos.org/6.10/os/$basearch/
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6

# Released updates
[updates]
name=CentOS-$releasever - Updates
#mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=updates&infra=$infra
baseurl=http://vault.centos.org/6.10/updates/$basearch/
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6

# Additional packages that may be useful
[extras]
name=CentOS-$releasever - Extras
# mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=extras&infra=$infra
baseurl=http://vault.centos.org/6.10/extras/$basearch/
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6

# Additional packages that extend functionality of existing packages
[centosplus]
name=CentOS-$releasever - Plus
# mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=centosplus&infra=$infra
baseurl=http://vault.centos.org/6.10/centosplus/$basearch/
gpgcheck=1
enabled=0
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6

# Contrib - packages by Centos Users
[contrib]
name=CentOS-$releasever - Contrib
# mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=contrib&infra=$infra
baseurl=http://vault.centos.org/6.10/contrib/$basearch/
gpgcheck=1
enabled=0
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6
```

Now update again:

```bash
[root@quickstart /]# yum update
```

However, this takes ages, so maybe just install on a "need to know basis" e.g.

```bash
[root@quickstart /]# yum install wget -y
```

### Java

Remove the old version of Java:

```bash
[root@quickstart /]# java -version
java version "1.7.0_67"

[root@quickstart /]# yum remove java -y
```

Install Java 8 (to be used with Spark 3 that supports Scala 2.12):

```bash
[root@quickstart /]# yum install java-1.8.0-openjdk

[root@quickstart /]# export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk.x86_64
```

### Scala

Install Scala 2.12:

```bash
[root@quickstart ~]# wget https://downloads.lightbend.com/scala/2.12.12/scala-2.12.12.tgz --no-check-certificate

[root@quickstart ~]# tar xvf scala-2.12.12.tgz

[root@quickstart ~]# mv scala-2.12.12 /usr/lib

[root@quickstart ~]# ln -s /usr/lib/scala-2.12.12 /usr/lib/scala

[root@quickstart ~]# export PATH=$PATH:/usr/lib/scala/bin

[root@quickstart ~]# scala -version
Scala code runner version 2.12.12 -- Copyright 2002-2020, LAMP/EPFL and Lightbend, Inc.
```

### Spark

```bash
[root@quickstart /]# hadoop version
Hadoop 2.6.0-cdh5.16.2
```

As we have Hadoop version 2.6, the most up to date version of Spark we can install is version 2.4.2 which uses Scala 2.12:

```bash
[root@quickstart /]# wget https://archive.apache.org/dist/spark/spark-2.4.2/spark-2.4.2-bin-hadoop2.6.tgz

[root@quickstart /]# tar -xvf spark-2.4.2-bin-hadoop2.6.tgz

[root@quickstart /]# mv spark-2.4.2-bin-hadoop2.6 /usr/local/spark
```

We now need to make this the **default** Spark by updating 3 files:
- /usr/bin/pyspark
- /usr/bin/spark-shell
- /usr/bin/spark-submit

```bash
# Change /usr/lib/spark to /usr/local/spark
[root@quickstart ~]# vim /usr/bin/pyspark

# Change /usr/lib/spark to /usr/local/spark
[root@quickstart ~]# vim /usr/bin/spark-shell

# Change /usr/lib/spark to /usr/local/spark
[root@quickstart ~]# vim /usr/bin/spark-submit
```

And we are in business:

```bash
[root@quickstart /]# spark-shell
20/12/30 17:55:08 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
Spark context Web UI available at http://quickstart.cloudera:4040
Spark context available as 'sc' (master = local[*], app id = local-1609350919424).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 2.4.2
      /_/

Using Scala version 2.12.8 (OpenJDK 64-Bit Server VM, Java 1.8.0_275)
```

### New Docker Image

We "save" this instance as an updated image:

```bash
➜ docker container ls -a
CONTAINER ID   IMAGE                 COMMAND                  CREATED          STATUS          PORTS                                            NAMES
a46e77febfe2   cloudera/quickstart   "/usr/bin/docker-qui…"   15 minutes ago   Up 15 minutes   0.0.0.0:7180->7180/tcp, 0.0.0.0:8888->8888/tcp   heuristic_bardeen
```

```bash
➜ docker commit heuristic_bardeen
sha256:17def59205cec7a1c4e817aa1f3ce18f937ac24dc78b6ed1a66d293ce57a681a
```

```bash
➜ docker image ls
REPOSITORY        TAG            IMAGE ID       CREATED          SIZE
<none>            <none>         17def59205ce   23 seconds ago   7.51G
```

```bash
➜ docker tag 17def59205ce davidainslie/cloudera
```

```bash
➜ docker image ls
REPOSITORY                    TAG               IMAGE ID       CREATED              SIZE
davidainslie/cloudera         latest            17def59205ce   About a minute ago   7.51GB
```

```bash
➜ docker push davidainslie/cloudera
```

### New Run

We can stop the previous container and restart with our new image:

```bash
docker run --hostname=quickstart.cloudera --privileged=true -it -p 7180:7180 -p 8888:8888 davidainslie/cloudera /usr/bin/docker-quickstart
```

Double check that all is good:

```bash
scala> val sample = Seq(1 -> "spark", 2 -> "Big Data")
sampleSeq: Seq[(Int, String)] = List((1,spark), (2,Big Data))

scala> val df = spark.createDataFrame(sample).toDF("course id", "course name")
df: org.apache.spark.sql.DataFrame = [course id: int, course name: string]

scala> df.show()
+---------+-----------+
|course id|course name|
+---------+-----------+
|        1|      spark|
|        2|   Big Data|
+---------+-----------+
```

## Using Hive

We do the following to avoid weird issues:

```bash
[root@quickstart /]# hdfs dfs -rm -r /tmp/hive
Deleted /tmp/hive

[root@quickstart /]# rm -rf /tmp/hive
```

From the command line in Docker, enter the Hive shell and the following SQL:

```bash
[root@quickstart /]# hive

hive> create database if not exists coursesdb;

hive> use coursesdb;

hive> create table if not exists coursesdb.courses(course_id string, course_name string, author_name string, no_of_reviews string);

hive> show tables;

hive> insert into coursesdb.courses values ('1', 'Java', 'Backwards', '45');
insert into coursesdb.courses values ('2', 'Java', 'Backwards', '56');
insert into coursesdb.courses values ('3', 'Big Data', 'Backwards', '100');
insert into coursesdb.courses values ('4', 'Linux', 'Backwards', '100');
insert into coursesdb.courses values ('5', 'Microservices', 'Backwards', '100');
insert into coursesdb.courses values ('6', 'CMS', '', '100');
insert into coursesdb.courses values ('7', 'Python', 'Backwards', '');
insert into coursesdb.courses values ('8', 'CMS', 'Backwards', '56');
insert into coursesdb.courses values ('9', 'Dot Net', 'Backwards', '34');
insert into coursesdb.courses values ('10', 'Ansible', 'Backwards', '123');
insert into coursesdb.courses values ('11', 'Jenkins', 'Backwards', '32');
insert into coursesdb.courses values ('12', 'Chef', 'Backwards', '121');
insert into coursesdb.courses values ('13', 'Go Lang', '', '105');

hive> alter table coursesdb.courses set tblproperties('serialization.null.format' = '');

hive> select * from coursesdb.courses;
```

We do the following as **cloudera** user:

```bash
[root@quickstart /]# su cloudera
[cloudera@quickstart /]$ sudo spark-shell
```

Note we run **spark-shell** with sudo to avoid Hive issues.

```bash
scala> val courses = spark.sql("select * from coursesdb.courses")
```

## Spark Submit

We have a simple [example](../src/main/scala/com/backwards/spark/SparkSubmitExample.scala) to run on our Clouder cluster (though it only has one node):

On host:

```bash
# Build JAR:
➜ sbt -J-Xms2048m -J-Xmx2048m -J-DmainClass=com.backwards.spark.SparkSubmitExample big-data-hadoop-spark/clean big-data-hadoop-spark/assembly

# Boot our Cloudera Docker image (named):
➜ docker run --name cloudera --hostname=quickstart.cloudera --privileged=true -it -p 7180:7180 -p 8888:8888 davidainslie/cloudera /usr/bin/docker-quickstart

# Copy the generated JAR into the running container (e.g. from root project):
➜ docker cp courses/big-data-hadoop-spark/target/scala-2.12/big-data-hadoop-spark.jar cloudera:/
````

On Docker instance:

```bash
# Submit JAR
[root@quickstart /]# cd /root
[root@quickstart /]# export HADOOP_CONF_DIR=/usr/lib/hadoop
[root@quickstart /]# export YARN_CONF_DIR=/usr/lib/hadoop
[root@quickstart /]# spark-submit --class com.backwards.spark.SparkSubmitExample --master yarn --deploy-mode client big-data-hadoop-spark.jar
```

NOTE

We can do all the above locally, so long as you have Spark installed e.g. with Homebrew:

```bash
brew install apache-spark
```

Then we can submit our JAR:

```bash
➜ spark-submit --class com.backwards.spark.SparkSubmitExample big-data-hadoop-spark.jar
```