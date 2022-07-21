package io.github.reugn.aerospike.scala

import com.aerospike.client.listener.RecordSequenceListener
import com.aerospike.client.query.KeyRecord
import com.aerospike.client.{AerospikeException, Key, Record}

class QueryRecordSequenceListener extends RecordSequenceListener {

  private val recordSet = new RecordSet

  override def onRecord(key: Key, record: Record): Unit = {
    if (record != null) {
      recordSet.put(new KeyRecord(key, record))
    }
  }

  override def onSuccess(): Unit = {
    recordSet.close()
  }

  override def onFailure(exception: AerospikeException): Unit = {
    recordSet.closeExceptionally(exception)
  }

  def getRecordSet: RecordSet = recordSet
}
