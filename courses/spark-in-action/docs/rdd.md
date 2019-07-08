# Resilient Distributed Dataset

The RDD is the fundamental abstraction in Spark. It represents a collection of elements that is:

- *Immutable* (read-only) 
- *Resilient* (fault-tolerant) 
- *Distributed* (dataset spread out to more than one node) 

There are two types of RDD operations: transformations and actions.

- **Transformations** (e.g. filter, map) are operations that produce a new RDD by performing
  some useful data manipulation on another RDD.
- **Actions** (e.g. count, foreach, collect) trigger a computation in order to return the result to the calling program or
  to perform some actions on an RDD’s elements.