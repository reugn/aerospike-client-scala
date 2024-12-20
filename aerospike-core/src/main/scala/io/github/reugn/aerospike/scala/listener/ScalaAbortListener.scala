package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.AbortStatus
import com.aerospike.client.listener.AbortListener

class ScalaAbortListener extends AbortListener with PromiseLike[AbortStatus] {

  override def onSuccess(status: AbortStatus): Unit = {
    success(status)
  }
}
