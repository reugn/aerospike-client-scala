package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.RecordListener
import com.aerospike.client.{AerospikeException, Key, Record}

class ScalaRecordListener extends RecordListener with PromiseLike[Record] {

  override def onSuccess(key: Key, record: Record): Unit = {
    success(record)
  }

  override def onFailure(exception: AerospikeException): Unit = {
    failure(exception)
  }
}
