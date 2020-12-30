# Application JAR

To submit to a (cloud) cluster we need to build JAR (fully assembled):

```bash
sbt -J-Xms2048m -J-Xmx2048m -J-DmainClass=com.backwards.spark._4 master-spark/clean master-spark/assembly
```

Here we stipulate which **main class** to use and which project is to be assembled.
Note the extra "memory" settings as I've seen assembly fail due to running out of memory.

Due to the fact that we are inside a multi-module project and so there are many main classes, the build has been set up that the **mainClass** system property must be provided when assembling.