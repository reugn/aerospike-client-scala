package io.github.reugn.aerospike.scala

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, PartitionFilter, Statement}
import com.aerospike.client.task.ExecuteTask
import com.typesafe.config.Config
import io.github.reugn.aerospike.scala.listener._

import java.util.Calendar
import scala.concurrent.{ExecutionContext, Future}

class AerospikeHandler(protected val client: AerospikeClient)(implicit ec: ExecutionContext)
  extends AsyncHandler[Future]
    with StreamHandler2[Source] {

  override def put(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Key] = {
    val listener = new ScalaWriteListener
    client.put(null, listener, policy, key, bins: _*)
    listener.future
  }

  override def append(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Key] = {
    val listener = new ScalaWriteListener
    client.append(null, listener, policy, key, bins: _*)
    listener.future
  }

  override def prepend(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Key] = {
    val listener = new ScalaWriteListener
    client.prepend(null, listener, policy, key, bins: _*)
    listener.future
  }

  override def add(key: Key, bins: Bin*)(implicit policy: WritePolicy): Future[Key] = {
    val listener = new ScalaWriteListener
    client.add(null, listener, policy, key, bins: _*)
    listener.future
  }

  override def delete(key: Key)(implicit policy: WritePolicy): Future[Boolean] = {
    val listener = new ScalaDeleteListener
    client.delete(null, listener, policy, key)
    listener.future
  }

  override def truncate(ns: String, set: String, beforeLastUpdate: Option[Calendar] = None)
                       (implicit policy: InfoPolicy): Future[Unit] = {
    Future(client.truncate(policy, ns, set, beforeLastUpdate.orNull))
  }

  override def touch(key: Key)(implicit policy: WritePolicy): Future[Key] = {
    val listener = new ScalaWriteListener
    client.touch(null, listener, policy, key)
    listener.future
  }

  override def exists(key: Key)(implicit policy: Policy): Future[Boolean] = {
    val listener = new ScalaExistsListener
    client.exists(null, listener, policy, key)
    listener.future
  }

  override def existsBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Future[Seq[Boolean]] = {
    val listener = new ScalaExistsArrayListener
    client.exists(null, listener, policy, keys.toArray)
    listener.future
  }

  override def get(key: Key, binNames: String*)(implicit policy: Policy): Future[Record] = {
    val listener = new ScalaRecordListener
    if (binNames.toArray.length > 0)
      client.get(null, listener, policy, key, binNames: _*)
    else
      client.get(null, listener, policy, key)
    listener.future
  }

  override def getHeader(key: Key)(implicit policy: Policy): Future[Record] = {
    val listener = new ScalaRecordListener
    client.getHeader(null, listener, policy, key)
    listener.future
  }

  override def getBatch(keys: Seq[Key], binNames: String*)(implicit policy: BatchPolicy): Future[Seq[Record]] = {
    val listener = new ScalaRecordArrayListener
    if (binNames.toArray.length > 0)
      client.get(null, listener, policy, keys.toArray, binNames: _*)
    else
      client.get(null, listener, policy, keys.toArray)
    listener.future
  }

  override def getHeaderBatch(keys: Seq[Key])(implicit policy: BatchPolicy): Future[Seq[Record]] = {
    val listener = new ScalaRecordArrayListener
    client.getHeader(null, listener, policy, keys.toArray)
    listener.future
  }

  override def operate(key: Key, operations: Operation*)(implicit policy: WritePolicy): Future[Record] = {
    val listener = new ScalaRecordListener
    client.operate(null, listener, policy, key, operations: _*)
    listener.future
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
    client.scanAll(null, listener, policy, ns, set)
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
    client.scanPartitions(null, listener, policy, filter, ns, set)
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
    apply(new ClientPolicy(), hostname, port)

  def apply(policy: ClientPolicy, hostname: String, port: Int)(implicit ec: ExecutionContext): AerospikeHandler =
    new AerospikeHandler(new AerospikeClient(policy.withEventLoops(), hostname, port))

  def apply(policy: ClientPolicy, hosts: Seq[Host])(implicit ec: ExecutionContext): AerospikeHandler =
    new AerospikeHandler(new AerospikeClient(policy.withEventLoops(), hosts: _*))
}
