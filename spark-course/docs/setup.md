# Setup

## Optional Course Setup

The course itself is a gradle project - the following is optional and only related to the course I have [forked](https://github.com/davidainslie/scala-spark-tutorial) and can be cloned from Github.

```bash
brew install gradle
```

Within the **root** of the course module:

```bash
$ gradle wrapper --gradle-version 4.10.2

$ ./gradlew idea
```

With the above, we can run the course, separately, as a gradle project i.e. has to be separate as overall this is a sbt project.