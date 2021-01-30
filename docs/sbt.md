# SBT

## Package a submodule

e.g.
```bash
sbt "project big-data-with-spark-emr" package
```

## Assemble a submodule

e.g.
```bash
sbt "project big-data-with-spark-emr" assembly
```

Built JARs can be found under `<submodule>/target/scala-2.12/`

However, a better `assembly` (because of `extras` in `build.sbt`) would be for example:
```bash
sbt -J-Xms2048m -J-Xmx2048m -J-DmainClass=com.backwards.spark.demo.App demo-git-actions/assembly
```