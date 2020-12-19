# Hadoop

Hadoop is a framework for **Distributed Storage** and **Distributed Computing**. The Hadoop framework in a nutshell:

![Hadoop fraework](images/hadoop-framework.png)

Resource management and job scheduling are handled by **YARN**.

**Google Cloud Dataproc** is a fully managed cloud service for running **Apache Spark** and **Apache Hadoop**.

![Create cluster](images/create-cluster.png)

## Hive

**Hive** is a SQL like query tool to analyze data stored in **HDFS**. The layers are:

|   Hive    |
| :-------: |
| MapReduce |
|   YARN    |
|   HDFS    |

With our Dataproc cluster running and SSH onto master:

![SSH master](images/ssh-master.png)

Example of the new console:

```bash
Connected, host fingerprint: ssh-rsa 0 20:6C:53:08:CE:AE:EB:B0:D2:C6:D1:91:4B:A7
:09:8F:C3:29:B9:00:03:EF:54:A9:FE:9F:46:0F:4F:9F:8D:92
Linux my-dataproc-cluster-m 5.8.0-0.bpo.2-amd64 #1 SMP Debian 5.8.10-1~bpo10+1 (
2020-09-26) x86_64
The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.
Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.

dainslie@my-dataproc-cluster-m:~$ pwd
/home/dainslie
```

Pull in the file [retailstore.csv](../src)