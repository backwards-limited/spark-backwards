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