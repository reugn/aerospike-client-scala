package io.github.reugn.aerospike.scala

import com.aerospike.client.query.KeyRecord
import com.aerospike.client.{AerospikeException, Key, Record, ScanCallback}

import scala.collection.mutable.ArrayBuffer

class RecordScanCallback() extends ScanCallback {
  private val recordSet = ArrayBuffer[KeyRecord]()

  @throws[AerospikeException]
  override def scanCallback(key: Key, record: Record): Unit = {
    recordSet.append(new KeyRecord(key, record))
  }

  def getRecordSet: List[KeyRecord] = recordSet.toList
}

object RecordScanCallback {

  def apply(): RecordScanCallback = new RecordScanCallback()
}
