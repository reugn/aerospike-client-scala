package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.BatchOperateListListener
import com.aerospike.client.{AerospikeException, BatchRecord}

import java.util

class ScalaBatchOperateListListener extends BatchOperateListListener with PromiseLike[Boolean] {

  override def onSuccess(records: util.List[BatchRecord], status: Boolean): Unit = {
    success(status)
  }

  override def onFailure(exception: AerospikeException): Unit = {
    failure(exception)
  }
}
