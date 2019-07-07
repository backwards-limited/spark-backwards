# Setup

Apologies I shall only cover **Mac** - One day I may include Linux and Windows.

Install [Homebrew](https://brew.sh) for easy package management on Mac:

```bash
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

Installation essentials:

```bash
brew cask install virtualbox
brew cask install vagrant
brew cask install vagrant-manager
brew install scala
brew install sbt
brew install scalacenter/bloop/bloop
brew install apache-spark
brew install httpie
```

The [Spark in Action](../courses/spark-in-action) module uses a VM for Spark created by the authors of [Spark in Action](https://www.manning.com/books/spark-in-action):

```bash
$ mkdir spark-vm

$ cd spark-vm

$ wget https://raw.githubusercontent.com/spark-in-action/first-edition/master/spark-in-action-box.json

$ vagrant plugin update

$ vagrant box add spark-in-action-box.json
```

To use it, initialize the Vagrant VM in the current directory by issuing this command:

```bash
$ vagrant init manning/spark-in-action
```

And finally start the VM:

```bash
$ vagrant up
```

To power off the VM, issue the following command:

```bash
$ vagrant halt
```

This will stop the machine but preserve your work.

If you wish to completely remove the VM and free up its space, you need to *destroy* it:

```bash
$ vagrant destroy
```

You can also remove the downloaded Vagrant box, which was used to create the VM, with this command:

```bash
$ vagrant box remove manning/spark-in-action
```

With the VM up and running, you can log in to the VM:

```bash
$ vagrant ssh
```

OR

```bash
$ ssh spark@192.168.10.2
```

i.e. providing the IP address configured for the spark-in-action VM.

- username: spark
- password: spark

NOTE Spark is available from the **/usr/local/spark** folder, which is a symlink pointing to **/opt/spark-2.0.0-bin-hadoop2.7**, where the Spark binary archive was unpacked. Upon installing newer versions of Spark we can see:

```bash
spark@spark-in-action:~$ ls /opt | grep spark
spark-1.6.1-bin-hadoop2.6
spark-2.0.0-bin-hadoop2.7
```

If desired, we can change from say version 2.0.0 to 1.6.1 by removing and recreating the symlink:

```bash
spark@spark-in-action:~$ sudo rm -f /usr/local/spark

spark@spark-in-action:~$ sudo ln -s /opt/spark-1.6.1-bin-hadoop2.6 /usr/local/spark
```

Double check the **SPARK_HOME** environment variable is set:

```bash
spark@spark-in-action:~$ export | grep SPARK
declare -x SPARK_HOME="/usr/local/spark"
```

P.S. The book comes with a git repo:

```bash
spark@spark-in-action:~$ git clone https://github.com/spark-in-action/first-edition
```

