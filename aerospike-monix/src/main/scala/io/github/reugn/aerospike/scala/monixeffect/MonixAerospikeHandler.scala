package io.github.reugn.aerospike.scala.monixeffect

import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, Statement}
import com.aerospike.client.task.ExecuteTask
import com.typesafe.config.Config
import io.github.reugn.aerospike.scala._
import io.github.reugn.aerospike.scala.model.QueryStatement
import monix.eval.Task
import monix.reactive.Observable

import java.util.Calendar
import scala.collection.JavaConverters.seqAsJavaListConverter

class MonixAerospikeHandler(protected val client: IAerospikeClient)
  extends AsyncHandler[Task]
    with StreamHandler1[Observable] {

  override def put(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    Task(client.put(policy, key, bins: _*)).map(_ => key)
  }

  override def append(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    Task(client.append(policy, key, bins: _*)).map(_ => key)
  }

  override def prepend(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    Task(client.prepend(policy, key, bins: _*)).map(_ => key)
  }

  override def add(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Key] = {
    Task(client.add(policy, key, bins: _*)).map(_ => key)
  }

  override def delete(key: Key)(implicit policy: WritePolicy): Task[Boolean] = {
    Task(client.delete(policy, key))
  }

  override def deleteBatch(keys: Seq[Key])
                          (implicit policy: BatchPolicy, batchDeletePolicy: BatchDeletePolicy): Task[BatchResults] = {
    Task(client.delete(policy, batchDeletePolicy, keys.toArray))
  }

  override def truncate(ns: String, set: String, beforeLastUpdate: Option[Calendar] = None)
                       (implicit policy: InfoPolicy): Task[Unit] = {
    Task(client.truncate(policy, ns, set, beforeLastUpdate.orNull))
  }

  override def touch(key: Key)(implicit policy: WritePolicy): Task[Key] = {
    Task(client.touch(policy, key)).map(_ => key)
  }

  override def exists(key: Key)(implicit policy: Policy): Task[Boolean] = {
    Task(client.exists(policy, key))
  }

  override def existsBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Task[Seq[Boolean]] = {
    Task(client.exists(policy, keys.toArray)).map(_.toIndexedSeq)
  }

  override def get(key: Key, binNames: String*)(implicit policy: Policy): Task[Record] = {
    Task {
      if (binNames.toArray.length > 0)
        client.get(policy, key, binNames: _*)
      else
        client.get(policy, key)
    }
  }

  override def getBatch(keys: Seq[Key], binNames: String*)(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    Task {
      if (binNames.toArray.length > 0)
        client.get(policy, keys.toArray, binNames: _*)
      else
        client.get(policy, keys.toArray)
    } map {
      _.toIndexedSeq
    }
  }

  override def getBatchOp(keys: Seq[Key], operations: Operation*)(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    Task(client.get(policy, keys.toArray, operations: _*))
  }

  override def getHeader(key: Key)(implicit policy: Policy): Task[Record] = {
    Task(client.getHeader(policy, key))
  }

  override def getHeaderBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    Task(client.getHeader(policy, keys.toArray)).map(_.toIndexedSeq)
  }

  override def operate(key: Key, operations: Operation*)(implicit policy: WritePolicy): Task[Record] = {
    Task(client.operate(policy, key, operations: _*))
  }

  override def operateBatch(keys: Seq[Key], operations: Operation*)
                           (implicit policy: BatchPolicy, batchWritePolicy: BatchWritePolicy): Task[BatchResults] = {
    Task(client.operate(policy, batchWritePolicy, keys.toArray, operations: _*))
  }

  override def operateBatchRecord(records: Seq[BatchRecord])(implicit policy: BatchPolicy): Task[Boolean] = {
    Task(client.operate(policy, records.asJava))
  }

  override def scanNodeName(nodeName: String, ns: String, set: String, binNames: String*)
                           (implicit policy: ScanPolicy): Task[List[KeyRecord]] = {
    Task {
      val callback = RecordScanCallback()
      client.scanNode(policy, nodeName, ns, set, callback, binNames: _*)
      callback.getRecordSet
    }
  }

  override def scanNode(node: Node, ns: String, set: String, binNames: String*)
                       (implicit policy: ScanPolicy): Task[List[KeyRecord]] = {
    Task {
      val callback = RecordScanCallback()
      client.scanNode(policy, node, ns, set, callback, binNames: _*)
      callback.getRecordSet
    }
  }

  override def execute(statement: Statement, operations: Operation*)
                      (implicit policy: WritePolicy): Task[ExecuteTask] = {
    Task(client.execute(policy, statement, operations: _*))
  }

  override def info(node: Node, name: String): Task[String] = {
    Task(Info.request(node, name))
  }

  override def query(statement: QueryStatement)
                    (implicit policy: QueryPolicy): Observable[KeyRecord] = {
    val listener = new QueryRecordSequenceListener
    statement.partitionFilter match {
      case Some(partitionFilter) =>
        client.queryPartitions(null, listener, policy, statement.statement, partitionFilter)
      case None =>
        client.query(null, listener, policy, statement.statement)
    }
    Observable.fromIterator(Task(listener.getRecordSet.iterator))
  }
}

object MonixAerospikeHandler {

  import io.github.reugn.aerospike.scala.Policies.ClientPolicyImplicits._

  def apply(client: IAerospikeClient): MonixAerospikeHandler =
    new MonixAerospikeHandler(client)

  def apply(config: Config): MonixAerospikeHandler =
    new MonixAerospikeHandler(AerospikeClientBuilder(config).build())

  def apply(hostname: String, port: Int): MonixAerospikeHandler =
    apply(new ClientPolicy(), hostname, port)

  def apply(policy: ClientPolicy, hostname: String, port: Int): MonixAerospikeHandler =
    new MonixAerospikeHandler(new AerospikeClient(policy.withEventLoops(), hostname, port))

  def apply(policy: ClientPolicy, hosts: Seq[Host]): MonixAerospikeHandler =
    new MonixAerospikeHandler(new AerospikeClient(policy.withEventLoops(), hosts: _*))
}
