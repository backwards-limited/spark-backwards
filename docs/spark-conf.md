# SparkConf

Example:

```scala
val conf: SparkConf = new SparkConf().setAppName("my-app").setMaster("local[2]")
```

local[2] => 2 cores
local[*] => all available cores
local    => 1 core