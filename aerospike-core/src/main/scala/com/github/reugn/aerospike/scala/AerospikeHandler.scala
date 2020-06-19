package com.github.reugn.aerospike.scala

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, PartitionFilter, Statement}
import com.aerospike.client.task.ExecuteTask
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class AerospikeHandler(protected val client: AerospikeClient)(implicit ec: ExecutionContext)
  extends AsyncHandler[Future]
    with StreamHandler[Source] {

  override def put(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Unit] = {
    Future(client.put(policy, key, bins: _*))
  }

  override def append(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Unit] = {
    Future(client.append(policy, key, bins: _*))
  }

  override def prepend(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Unit] = {
    Future(client.prepend(policy, key, bins: _*))
  }

  override def add(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Unit] = {
    Future(client.add(policy, key, bins: _*))
  }

  override def delete(key: Key)(implicit policy: WritePolicy): Future[Boolean] = {
    Future(client.delete(policy, key))
  }

  override def truncate(ns: String, set: String, beforeLastUpdate: Option[Calendar] = None)
                       (implicit policy: InfoPolicy): Unit = {
    Future(client.truncate(policy, ns, set, beforeLastUpdate.orNull))
  }

  override def touch(key: Key)(implicit policy: WritePolicy): Future[Unit] = {
    Future(client.touch(policy, key))
  }

  override def exists(key: Key)(implicit policy: Policy): Future[Boolean] = {
    Future(client.exists(policy, key))
  }

  override def existsBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Future[Seq[Boolean]] = {
    Future(client.exists(policy, keys.toArray))
  }

  override def get(key: Key, binNames: String*)(implicit policy: Policy): Future[Record] = {
    Future {
      if (binNames.toArray.length > 0)
        client.get(policy, key, binNames: _*)
      else
        client.get(policy, key)
    }
  }

  override def getHeader(key: Key)(implicit policy: Policy): Future[Record] = {
    Future(client.getHeader(policy, key))
  }

  override def getBatch(keys: Seq[Key], binNames: String*)(implicit policy: BatchPolicy): Future[Seq[Record]] = {
    Future {
      if (binNames.toArray.length > 0)
        client.get(policy, keys.toArray, binNames: _*)
      else
        client.get(policy, keys.toArray)
    } map {
      _.toSeq
    }
  }

  override def getHeaderBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Future[Seq[Record]] = {
    Future(client.getHeader(policy, keys.toArray))
  }

  override def operate(key: Key, operations: Operation*)(implicit policy: WritePolicy): Future[Record] = {
    Future(client.operate(policy, key, operations: _*))
  }

  override def execute(statement: Statement, operations: Operation*)
                      (implicit policy: WritePolicy): Future[ExecuteTask] = {
    Future(client.execute(policy, statement, operations: _*))
  }

  override def info(node: Node, name: String): Future[String] = {
    Future(Info.request(node, name))
  }

  override def scanAll(ns: String, set: String, binNames: String*)
                      (implicit policy: ScanPolicy): Source[KeyRecord, NotUsed] = {
    val listener = new ScanRecordSequenceListener
    client.scanAll(client.getCluster.eventLoops.get(0), listener, policy, ns, set)
    Source.fromGraph(new KeyRecordSource(listener.getRecordSet.iterator))
  }

  override def scanNodeName(nodeName: String, ns: String, set: String, binNames: String*)
                           (implicit policy: ScanPolicy): Future[List[KeyRecord]] = {
    Future {
      val callback = RecordScanCallback()
      client.scanNode(policy, nodeName, ns, set, callback, binNames: _*)
      callback.getRecordSet
    }
  }

  override def scanNode(node: Node, ns: String, set: String, binNames: String*)
                       (implicit policy: ScanPolicy): Future[List[KeyRecord]] = {
    Future {
      val callback = RecordScanCallback()
      client.scanNode(policy, node, ns, set, callback, binNames: _*)
      callback.getRecordSet
    }
  }

  override def scanPartitions(filter: PartitionFilter, ns: String, set: String, binNames: String*)
                             (implicit policy: ScanPolicy): Source[KeyRecord, NotUsed] = {
    val listener = new ScanRecordSequenceListener
    client.scanPartitions(client.getCluster.eventLoops.get(0), listener, policy, filter, ns, set)
    Source.fromGraph(new KeyRecordSource(listener.getRecordSet.iterator))
  }
}

object AerospikeHandler {

  import Policies.ClientPolicyImplicits._

  def apply(client: AerospikeClient)(implicit ec: ExecutionContext): AerospikeHandler =
    new AerospikeHandler(client)

  def apply(config: Config)(implicit ec: ExecutionContext): AerospikeHandler =
    new AerospikeHandler(AerospikeClientBuilder(config).build())

  def apply(hostname: String, port: Int)(implicit ec: ExecutionContext): AerospikeHandler =
    this (new ClientPolicy(), hostname, port)

  def apply(policy: ClientPolicy, hostname: String, port: Int)(implicit ec: ExecutionContext): AerospikeHandler =
    new AerospikeHandler(new AerospikeClient(policy.withEventLoops(), hostname, port))
}
