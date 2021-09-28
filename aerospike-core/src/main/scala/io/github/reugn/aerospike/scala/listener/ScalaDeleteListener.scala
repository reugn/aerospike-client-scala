package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.DeleteListener
import com.aerospike.client.{AerospikeException, Key}

class ScalaDeleteListener extends DeleteListener with PromiseLike[Boolean] {

  override def onSuccess(key: Key, existed: Boolean): Unit = {
    success(existed)
  }

  override def onFailure(exception: AerospikeException): Unit = {
    failure(exception)
  }
}
