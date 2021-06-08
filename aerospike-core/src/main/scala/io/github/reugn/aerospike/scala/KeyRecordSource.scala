package io.github.reugn.aerospike.scala

import akka.stream.stage.{GraphStageLogic, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import com.aerospike.client.query.KeyRecord

class KeyRecordSource(iterator: Iterator[KeyRecord]) extends DataSource[KeyRecord] {

  private val out: Outlet[KeyRecord] = Outlet("KeyRecordSource")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if (iterator.hasNext)
            push(out, iterator.next())
          else
            complete(out)
        }
      })
    }
  }

  override def shape: SourceShape[KeyRecord] = SourceShape(out)
}
