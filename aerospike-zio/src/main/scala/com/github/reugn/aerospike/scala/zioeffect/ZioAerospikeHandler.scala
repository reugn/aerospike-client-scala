package com.github.reugn.aerospike.scala.zioeffect

import java.util.Calendar

import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, PartitionFilter, Statement}
import com.aerospike.client.task.ExecuteTask
import com.github.reugn.aerospike.scala._
import com.typesafe.config.Config
import zio.Task
import zio.stream.ZStream

class ZioAerospikeHandler(protected val client: AerospikeClient)
  extends AsyncHandler[Task]
    with StreamHandler3[ZStream] {

  override def put(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Unit] = {
    Task(client.put(policy, key, bins: _*))
  }

  override def append(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Unit] = {
    Task(client.append(policy, key, bins: _*))
  }

  override def prepend(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Unit] = {
    Task(client.prepend(policy, key, bins: _*))
  }

  override def add(key: Key, bins: Bin*)(implicit policy: WritePolicy): Task[Unit] = {
    Task(client.add(policy, key, bins: _*))
  }

  override def delete(key: Key)(implicit policy: WritePolicy): Task[Boolean] = {
    Task(client.delete(policy, key))
  }

  override def truncate(ns: String, set: String, beforeLastUpdate: Option[Calendar] = None)
                       (implicit policy: InfoPolicy): Task[Unit] = {
    Task(client.truncate(policy, ns, set, beforeLastUpdate.orNull))
  }

  override def touch(key: Key)(implicit policy: WritePolicy): Task[Unit] = {
    Task(client.touch(policy, key))
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

  override def getHeader(key: Key)(implicit policy: Policy): Task[Record] = {
    Task(client.getHeader(policy, key))
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

  override def getHeaderBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    Task(client.getHeader(policy, keys.toArray)).map(_.toIndexedSeq)
  }

  override def operate(key: Key, operations: Operation*)(implicit policy: WritePolicy): Task[Record] = {
    Task(client.operate(policy, key, operations: _*))
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

  override def scanAll(ns: String, set: String, binNames: String*)
                      (implicit policy: ScanPolicy): ZStream[Any, Throwable, KeyRecord] = {
    val listener = new ScanRecordSequenceListener
    client.scanAll(null, listener, policy, ns, set)
    ZStream.fromIterator(listener.getRecordSet.iterator)
  }

  override def scanPartitions(filter: PartitionFilter, ns: String, set: String, binNames: String*)
                             (implicit policy: ScanPolicy): ZStream[Any, Throwable, KeyRecord] = {
    val listener = new ScanRecordSequenceListener
    client.scanPartitions(null, listener, policy, filter, ns, set)
    ZStream.fromIterator(listener.getRecordSet.iterator)
  }
}

object ZioAerospikeHandler {

  import Policies.ClientPolicyImplicits._

  def apply(client: AerospikeClient): ZioAerospikeHandler =
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
