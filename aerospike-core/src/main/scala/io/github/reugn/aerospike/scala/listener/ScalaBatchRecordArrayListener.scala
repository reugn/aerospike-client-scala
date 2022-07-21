package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.BatchRecordArrayListener
import com.aerospike.client.{AerospikeException, BatchRecord, BatchResults}

class ScalaBatchRecordArrayListener extends BatchRecordArrayListener with PromiseLike[BatchResults] {

  override def onSuccess(records: Array[BatchRecord], status: Boolean): Unit = {
    success(new BatchResults(records, status))
  }

  override def onFailure(records: Array[BatchRecord], exception: AerospikeException): Unit = {
    failure(exception)
  }
}
