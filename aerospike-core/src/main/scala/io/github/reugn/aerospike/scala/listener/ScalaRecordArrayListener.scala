package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.RecordArrayListener
import com.aerospike.client.{AerospikeException, Key, Record}

class ScalaRecordArrayListener extends RecordArrayListener with PromiseLike[Seq[Record]] {

  override def onSuccess(keys: Array[Key], records: Array[Record]): Unit = {
    success(records)
  }

  override def onFailure(exception: AerospikeException): Unit = {
    failure(exception)
  }
}
