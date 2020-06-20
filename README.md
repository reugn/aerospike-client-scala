# aerospike-client-scala
[![Build Status](https://travis-ci.com/reugn/aerospike-client-scala.svg?branch=master)](https://travis-ci.com/reugn/aerospike-client-scala)
[ ![Download](https://api.bintray.com/packages/reug/maven/aerospike-core/images/download.svg) ](https://bintray.com/reug/maven/aerospike-core/_latestVersion)

Idiomatic and reactive Scala client for [Aerospike](https://www.aerospike.com/) database.

## Modules
* `aerospike-core` provides standard Scala Future and Akka Streams implementation.
* `aerospike-monix` integrates with [Monix](https://monix.io/) library to support Monix effects.
* `aerospike-zio` integrates with [ZIO](https://zio.dev/) library to support ZIO effects.

## Getting started
Add bintray resolver:
```scala
resolvers += Resolver.bintrayRepo("reug", "maven")
```
Add aerospike-core module as a dependency in your project:
```scala
libraryDependencies += "com.github.reugn" %% "aerospike-core" % "<version>"
```
* replace `aerospike-core` with `aerospike-monix` or `aerospike-zio` if required.

## License
Licensed under the [Apache 2.0 License](./LICENSE).