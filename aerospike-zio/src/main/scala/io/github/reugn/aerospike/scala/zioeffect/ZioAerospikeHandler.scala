package io.github.reugn.aerospike.scala.zioeffect

import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, Statement}
import com.aerospike.client.task.ExecuteTask
import com.typesafe.config.Config
import io.github.reugn.aerospike.scala._
import io.github.reugn.aerospike.scala.model.QueryStatement
import zio.stream.ZStream
import zio.{Task, ZIO}

import java.util.Calendar
import scala.collection.JavaConverters.seqAsJavaListConverter

class ZioAerospikeHandler(protected val client: IAerospikeClient)
  extends AsyncHandler[Task]
    with StreamHandler3[ZStream] {

  override def put(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    ZIO.attemptBlocking(client.put(policy, key, bins: _*)).map(_ => key)
  }

  override def append(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    ZIO.attemptBlocking(client.append(policy, key, bins: _*)).map(_ => key)
  }

  override def prepend(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    ZIO.attemptBlocking(client.prepend(policy, key, bins: _*)).map(_ => key)
  }

  override def add(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    ZIO.attemptBlocking(client.add(policy, key, bins: _*)).map(_ => key)
  }

  override def delete(key: Key)(implicit policy: WritePolicy): Task[Boolean] = {
    ZIO.attemptBlocking(client.delete(policy, key))
  }

  override def deleteBatch(keys: Seq[Key])
                          (implicit policy: BatchPolicy, batchDeletePolicy: BatchDeletePolicy): Task[BatchResults] = {
    ZIO.attemptBlocking(client.delete(policy, batchDeletePolicy, keys.toArray))
  }

  override def truncate(ns: String, set: String, beforeLastUpdate: Option[Calendar] = None)
                       (implicit policy: InfoPolicy): Task[Unit] = {
    ZIO.attemptBlocking(client.truncate(policy, ns, set, beforeLastUpdate.orNull))
  }

  override def touch(key: Key)(implicit policy: WritePolicy): Task[Key] = {
    ZIO.attemptBlocking(client.touch(policy, key)).map(_ => key)
  }

  override def exists(key: Key)(implicit policy: Policy): Task[Boolean] = {
    ZIO.attemptBlocking(client.exists(policy, key))
  }

  override def existsBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Task[Seq[Boolean]] = {
    ZIO.attemptBlocking(client.exists(policy, keys.toArray)).map(_.toIndexedSeq)
  }

  override def get(key: Key, binNames: String*)(implicit policy: Policy): Task[Record] = {
    ZIO.attemptBlocking {
      if (binNames.toArray.length > 0)
        client.get(policy, key, binNames: _*)
      else
        client.get(policy, key)
    }
  }

  override def getBatch(keys: Seq[Key], binNames: String*)(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    ZIO.attemptBlocking {
      if (binNames.toArray.length > 0)
        client.get(policy, keys.toArray, binNames: _*)
      else
        client.get(policy, keys.toArray)
    } map {
      _.toIndexedSeq
    }
  }

  override def getBatchOp(keys: Seq[Key], operations: Operation*)(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    ZIO.attemptBlocking(client.get(policy, keys.toArray, operations: _*))
  }

  override def getHeader(key: Key)(implicit policy: Policy): Task[Record] = {
    ZIO.attemptBlocking(client.getHeader(policy, key))
  }

  override def getHeaderBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    ZIO.attemptBlocking(client.getHeader(policy, keys.toArray)).map(_.toIndexedSeq)
  }

  override def operate(key: Key, operations: Operation*)(implicit policy: WritePolicy): Task[Record] = {
    ZIO.attemptBlocking(client.operate(policy, key, operations: _*))
  }

  override def operateBatch(keys: Seq[Key], operations: Operation*)
                           (implicit policy: BatchPolicy, batchWritePolicy: BatchWritePolicy): Task[BatchResults] = {
    ZIO.attemptBlocking(client.operate(policy, batchWritePolicy, keys.toArray, operations: _*))
  }

  override def operateBatchRecord(records: Seq[BatchRecord])(implicit policy: BatchPolicy): Task[Boolean] = {
    ZIO.attemptBlocking(client.operate(policy, records.asJava))
  }

  override def scanNodeName(nodeName: String, ns: String, set: String, binNames: String*)
                           (implicit policy: ScanPolicy): Task[List[KeyRecord]] = {
    ZIO.attemptBlocking {
      val callback = RecordScanCallback()
      client.scanNode(policy, nodeName, ns, set, callback, binNames: _*)
      callback.getRecordSet
    }
  }

  override def scanNode(node: Node, ns: String, set: String, binNames: String*)
                       (implicit policy: ScanPolicy): Task[List[KeyRecord]] = {
    ZIO.attemptBlocking {
      val callback = RecordScanCallback()
      client.scanNode(policy, node, ns, set, callback, binNames: _*)
      callback.getRecordSet
    }
  }

  override def execute(statement: Statement, operations: Operation*)
                      (implicit policy: WritePolicy): Task[ExecuteTask] = {
    ZIO.attemptBlocking(client.execute(policy, statement, operations: _*))
  }

  override def info(node: Node, name: String): Task[String] = {
    ZIO.attemptBlocking(Info.request(node, name))
  }

  override def query(statement: QueryStatement)
                    (implicit policy: QueryPolicy): ZStream[Any, Throwable, KeyRecord] = {
    val listener = new QueryRecordSequenceListener
    statement.partitionFilter match {
      case Some(partitionFilter) =>
        client.queryPartitions(null, listener, policy, statement.statement, partitionFilter)
      case None =>
        client.query(null, listener, policy, statement.statement)
    }
    ZStream.fromIterator(listener.getRecordSet.iterator)
  }
}

object ZioAerospikeHandler {

  import Policies.ClientPolicyImplicits._

  def apply(client: IAerospikeClient): ZioAerospikeHandler =
    new ZioAerospikeHandler(client)

  def apply(config: Config): ZioAerospikeHandler =
    new ZioAerospikeHandler(AerospikeClientBuilder(config).build())

  def apply(hostname: String, port: Int): ZioAerospikeHandler =
    apply(new ClientPolicy(), hostname, port)

  def apply(policy: ClientPolicy, hostname: String, port: Int): ZioAerospikeHandler =
    new ZioAerospikeHandler(new AerospikeClient(policy.withEventLoops(), hostname, port))

  def apply(policy: ClientPolicy, hosts: Seq[Host]): ZioAerospikeHandler =
    new ZioAerospikeHandler(new AerospikeClient(policy.withEventLoops(), hosts: _*))
}
