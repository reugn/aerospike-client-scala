# aerospike-client-scala
[![Build](https://github.com/reugn/aerospike-client-scala/actions/workflows/build.yml/badge.svg)](https://github.com/reugn/aerospike-client-scala/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.reugn/aerospike-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.reugn/aerospike-core_2.12/)

Idiomatic and reactive Scala client for [Aerospike](https://www.aerospike.com/) database.

## Modules
* `aerospike-core` provides standard Scala Future and Akka Streams implementation.
* `aerospike-monix` integrates with [Monix](https://monix.io/) to support Monix effects.
* `aerospike-zio` integrates with [ZIO](https://zio.dev/) to support ZIO effects.

## Getting started
Add the `aerospike-core` module as a dependency in your project:
```scala
libraryDependencies += "io.github.reugn" %% "aerospike-core" % "<version>"
```
* replace `aerospike-core` with `aerospike-monix` or `aerospike-zio` if required.

## Build from Source
1. Clone the repository
2. Run `sbt clean +package`

This will create libraries for all modules for both Scala 2.12 and 2.13.

## License
Licensed under the [Apache 2.0 License](./LICENSE).
