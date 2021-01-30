# Release

Currently, released versions of this module can be added as a dependency from [JitPack](https://jitpack.io).

We can release submodules e.g. module `spark` has reusable functionality:

```bash
sbt "project spark; release with-defaults"
```