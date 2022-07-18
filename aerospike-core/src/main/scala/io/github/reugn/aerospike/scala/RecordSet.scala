package io.github.reugn.aerospike.scala

import com.aerospike.client.query.KeyRecord
import com.aerospike.client.{Key, Record}
import io.github.reugn.aerospike.scala.RecordSet.queueCapacity

import java.io.Closeable
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

object RecordSet {
  private val END: KeyRecord = new KeyRecord(null, null)
  private val queueCapacity = 256

  private class RecordSetIterator private[RecordSet](val recordSet: RecordSet)
    extends Iterator[KeyRecord] with Closeable {

    private var more: Boolean = recordSet.next

    override def hasNext: Boolean = more

    override def next(): KeyRecord = {
      val keyRecord: KeyRecord = recordSet.record
      more = recordSet.next
      keyRecord
    }

    override def close(): Unit = {
      recordSet.close()
    }
  }
}

final class RecordSet extends Iterable[KeyRecord] with Closeable {
  private val queue: BlockingQueue[KeyRecord] = new LinkedBlockingQueue[KeyRecord](queueCapacity)
  private var record: KeyRecord = _
  private var valid: Boolean = true
  @volatile private var exception: Exception = _

  def next: Boolean = {
    if (valid) {
      try record = queue.take()
      catch {
        case _: InterruptedException =>
          valid = false
      }
      if (record eq RecordSet.END) {
        if (exception != null) {
          throw exception
        }
        valid = false
      }
    }
    valid
  }

  def closeExceptionally(e: Exception): Unit = {
    exception = e
    close()
  }

  override def close(): Unit = {
    put(RecordSet.END)
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
