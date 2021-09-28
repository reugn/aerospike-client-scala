package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.ExistsListener
import com.aerospike.client.{AerospikeException, Key}

class ScalaExistsListener extends ExistsListener with PromiseLike[Boolean] {

  override def onSuccess(key: Key, exists: Boolean): Unit = {
    success(exists)
  }

  override def onFailure(exception: AerospikeException): Unit = {
    failure(exception)
  }
}
