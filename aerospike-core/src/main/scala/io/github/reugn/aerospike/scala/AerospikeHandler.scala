package io.github.reugn.aerospike.scala

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, Statement}
import com.aerospike.client.task.ExecuteTask
import com.typesafe.config.Config
import io.github.reugn.aerospike.scala.listener._
import io.github.reugn.aerospike.scala.model.QueryStatement

import java.util.Calendar
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.{ExecutionContext, Future}

class AerospikeHandler(protected val client: IAerospikeClient)(implicit ec: ExecutionContext)
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

  override def deleteBatch(keys: Seq[Key])
                          (implicit policy: BatchPolicy, batchDeletePolicy: BatchDeletePolicy): Future[BatchResults] = {
    val listener = new ScalaBatchRecordArrayListener
    client.delete(null, listener, policy, batchDeletePolicy, keys.toArray)
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

  override def getBatch(keys: Seq[Key], binNames: String*)(implicit policy: BatchPolicy): Future[Seq[Record]] = {
    val listener = new ScalaRecordArrayListener
    if (binNames.toArray.length > 0)
      client.get(null, listener, policy, keys.toArray, binNames: _*)
    else
      client.get(null, listener, policy, keys.toArray)
    listener.future
  }

  override def getBatchOp(keys: Seq[Key], operations: Operation*)(implicit policy: BatchPolicy): Future[Seq[Record]] = {
    val listener = new ScalaRecordArrayListener
    client.get(null, listener, policy, keys.toArray, operations: _*)
    listener.future
  }

  override def getHeader(key: Key)(implicit policy: Policy): Future[Record] = {
    val listener = new ScalaRecordListener
    client.getHeader(null, listener, policy, key)
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

  override def operateBatch(keys: Seq[Key], operations: Operation*)
                           (implicit policy: BatchPolicy, batchWritePolicy: BatchWritePolicy): Future[BatchResults] = {
    val listener = new ScalaBatchRecordArrayListener
    client.operate(null, listener, policy, batchWritePolicy, keys.toArray, operations: _*)
    listener.future
  }

  override def operateBatchRecord(records: Seq[BatchRecord])(implicit policy: BatchPolicy): Future[Boolean] = {
    val listener = new ScalaBatchOperateListListener
    client.operate(null, listener, policy, records.asJava)
    listener.future
  }

  override def commit(txn: Txn): Future[CommitStatus] = {
    val listener = new ScalaCommitListener
    client.commit(null, listener, txn)
    listener.future
  }

  override def abort(txn: Txn): Future[AbortStatus] = {
    val listener = new ScalaAbortListener
    client.abort(null, listener, txn)
    listener.future
  }

  override def execute(statement: Statement, operations: Operation*)
                      (implicit policy: WritePolicy): Future[ExecuteTask] = {
    Future(client.execute(policy, statement, operations: _*))
  }

  override def info(node: Node, commands: String*)(implicit policy: InfoPolicy): Future[Map[String, String]] = {
    val listener = new ScalaInfoListener
    client.info(null, listener, policy, node, commands: _*)
    listener.future
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

  override def query(statement: QueryStatement)
                    (implicit policy: QueryPolicy): Source[KeyRecord, NotUsed] = {
    val listener = new QueryRecordSequenceListener
    statement.partitionFilter match {
      case Some(partitionFilter) =>
        client.queryPartitions(null, listener, policy, statement.statement, partitionFilter)
      case None =>
        client.query(null, listener, policy, statement.statement)
    }
    Source.fromGraph(new KeyRecordSource(listener.getRecordSet.iterator))
  }
}

object AerospikeHandler {

  import Policies.ClientPolicyImplicits._

  def apply(client: IAerospikeClient)(implicit ec: ExecutionContext): AerospikeHandler =
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
