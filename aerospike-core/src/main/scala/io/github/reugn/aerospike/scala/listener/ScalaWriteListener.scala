package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.WriteListener
import com.aerospike.client.{AerospikeException, Key}

class ScalaWriteListener extends WriteListener with PromiseLike[Key] {

  override def onSuccess(key: Key): Unit = {
    success(key)
  }

  override def onFailure(exception: AerospikeException): Unit = {
    failure(exception)
  }
}
