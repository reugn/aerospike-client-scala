package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.ExistsArrayListener
import com.aerospike.client.{AerospikeException, Key}

class ScalaExistsArrayListener extends ExistsArrayListener with PromiseLike[Seq[Boolean]] {

  override def onSuccess(keys: Array[Key], exists: Array[Boolean]): Unit = {
    success(exists)
  }

  override def onFailure(exception: AerospikeException): Unit = {
    failure(exception)
  }
}
