package com.github.reugn.aerospike.scala

import akka.stream.SourceShape
import akka.stream.stage.GraphStage

abstract class DataSource[T] extends GraphStage[SourceShape[T]]