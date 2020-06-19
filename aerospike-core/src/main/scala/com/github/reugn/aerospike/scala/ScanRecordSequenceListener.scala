package com.github.reugn.aerospike.scala

import com.aerospike.client.listener.RecordSequenceListener
import com.aerospike.client.query.KeyRecord
import com.aerospike.client.{AerospikeException, Key, Record}

class ScanRecordSequenceListener extends RecordSequenceListener {

  private val recordSet = new RecordSet(ScanRecordSequenceListener.defaultCapacity)

  override def onRecord(key: Key, record: Record): Unit = recordSet.put(new KeyRecord(key, record))

  override def onSuccess(): Unit = recordSet.put(RecordSet.END)

  override def onFailure(exception: AerospikeException): Unit = recordSet.close()

  def getRecordSet: RecordSet = recordSet
}

object ScanRecordSequenceListener {
  private val defaultCapacity = 8192

  def apply(): ScanRecordSequenceListener = new ScanRecordSequenceListener()
}
