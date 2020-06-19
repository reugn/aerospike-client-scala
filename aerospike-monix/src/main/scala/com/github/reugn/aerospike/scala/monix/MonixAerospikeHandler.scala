package com.github.reugn.aerospike.scala.monix

import java.util.Calendar

import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, Statement}
import com.aerospike.client.task.ExecuteTask
import com.github.reugn.aerospike.scala.{AerospikeClientBuilder, AsyncHandler, Policies, RecordScanCallback}
import com.typesafe.config.Config
import monix.eval.Task

class MonixAerospikeHandler(protected val client: AerospikeClient) extends AsyncHandler[Task] {

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
                       (implicit policy: InfoPolicy): Unit = {
    Task(client.truncate(policy, ns, set, beforeLastUpdate.orNull))
  }

  override def touch(key: Key)(implicit policy: WritePolicy): Task[Unit] = {
    Task(client.touch(policy, key))
  }

  override def exists(key: Key)(implicit policy: Policy): Task[Boolean] = {
    Task(client.exists(policy, key))
  }

  override def existsBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Task[Seq[Boolean]] = {
    Task(client.exists(policy, keys.toArray))
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
      _.toSeq
    }
  }

  override def getHeaderBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Task[Seq[Record]] = {
    Task(client.getHeader(policy, keys.toArray))
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
}

object MonixAerospikeHandler {

  import Policies.ClientPolicyImplicits._

  def apply(client: AerospikeClient): MonixAerospikeHandler =
    new MonixAerospikeHandler(client)

  def apply(config: Config): MonixAerospikeHandler =
    new MonixAerospikeHandler(AerospikeClientBuilder(config).build())

  def apply(hostname: String, port: Int): MonixAerospikeHandler =
    this (new ClientPolicy(), hostname, port)

  def apply(policy: ClientPolicy, hostname: String, port: Int): MonixAerospikeHandler =
    new MonixAerospikeHandler(new AerospikeClient(policy.withEventLoops(), hostname, port))
}
