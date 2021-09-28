package io.github.reugn.aerospike.scala

import com.aerospike.client.query.KeyRecord
import com.aerospike.client.{Key, Record}

import java.io.Closeable
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

object RecordSet {
  val END: KeyRecord = new KeyRecord(null, null)

  private class RecordSetIterator private[RecordSet](val recordSet: RecordSet)
    extends Iterator[KeyRecord] with Closeable {

    private var more: Boolean = recordSet.next

    override def hasNext: Boolean = more

    override def next(): KeyRecord = {
      val kr: KeyRecord = recordSet.record
      more = recordSet.next
      kr
    }

    override def close(): Unit = {
      recordSet.close()
    }
  }

}

final class RecordSet(val capacity: Int) extends Iterable[KeyRecord] with Closeable {
  private val queue: BlockingQueue[KeyRecord] = new ArrayBlockingQueue[KeyRecord](capacity)
  private var record: KeyRecord = _
  private var valid: Boolean = true

  def next: Boolean = {
    try record = queue.take
    catch {
      case _: InterruptedException =>
        valid = false
        return false
    }
    if (record eq RecordSet.END) {
      valid = false
      false
    }
    else true
  }

  override def close(): Unit = {
    valid = false
  }

  override def iterator: Iterator[KeyRecord] = new RecordSet.RecordSetIterator(this)

  def getKey: Key = record.key

  def getRecord: Record = record.record

  def put(record: KeyRecord): Boolean = if (!valid) false
  else try {
    queue.put(record)
    true
  } catch {
    case _: InterruptedException =>
      if (valid) abort()
      false
  }

  protected def abort(): Unit = {
    valid = false
    queue.clear()
    while (true)
      if (queue.offer(RecordSet.END) && queue.poll() == null) return
  }
}
