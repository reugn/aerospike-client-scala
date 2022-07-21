package io.github.reugn.aerospike.scala

import com.aerospike.client._
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy._
import com.aerospike.client.query.{KeyRecord, Statement}
import com.aerospike.client.task.ExecuteTask

import java.util.Calendar
import scala.language.higherKinds

trait AsyncHandler[F[_]] {

  protected def client: IAerospikeClient

  def asJava: IAerospikeClient = client

  //-------------------------------------------------------
  // Write Record Operations
  //-------------------------------------------------------

  def put(key: Key, bins: Bin*)(implicit policy: WritePolicy = null): F[Key]

  //-------------------------------------------------------
  // String Operations
  //-------------------------------------------------------

  def append(key: Key, bins: Bin*)(implicit policy: WritePolicy = null): F[Key]

  def prepend(key: Key, bins: Bin*)(implicit policy: WritePolicy = null): F[Key]

  //-------------------------------------------------------
  // Arithmetic Operations
  //-------------------------------------------------------

  def add(key: Key, bins: Bin*)(implicit policy: WritePolicy = null): F[Key]

  //-------------------------------------------------------
  // Delete Operations
  //-------------------------------------------------------

  def delete(key: Key)(implicit policy: WritePolicy = null): F[Boolean]

  def deleteBatch(keys: Seq[Key])
                 (implicit policy: BatchPolicy = null, batchDeletePolicy: BatchDeletePolicy = null): F[BatchResults]

  def truncate(ns: String, set: String, beforeLastUpdate: Option[Calendar] = None)
              (implicit policy: InfoPolicy = null): F[Unit]

  //-------------------------------------------------------
  // Touch Operations
  //-------------------------------------------------------

  def touch(key: Key)(implicit policy: WritePolicy = null): F[Key]

  //-------------------------------------------------------
  // Existence-Check Operations
  //-------------------------------------------------------

  def exists(key: Key)(implicit policy: Policy = null): F[Boolean]

  def existsBatch(keys: Seq[Key])(implicit policy: BatchPolicy = null): F[Seq[Boolean]]

  //-------------------------------------------------------
  // Read Record Operations
  //-------------------------------------------------------

  def get(key: Key, binNames: String*)(implicit policy: Policy = null): F[Record]

  def getBatch(keys: Seq[Key], binNames: String*)(implicit policy: BatchPolicy = null): F[Seq[Record]]

  def getBatchOp(keys: Seq[Key], operations: Operation*)(implicit policy: BatchPolicy = null): F[Seq[Record]]

  def getHeader(key: Key)(implicit policy: Policy = null): F[Record]

  def getHeaderBatch(keys: Seq[Key])(implicit policy: BatchPolicy = null): F[Seq[Record]]

  //-------------------------------------------------------
  // Generic Database Operations
  //-------------------------------------------------------

  def operate(key: Key, operations: Operation*)(implicit policy: WritePolicy = null): F[Record]

  def operateBatch(keys: Seq[Key], operations: Operation*)
                  (implicit policy: BatchPolicy = null, batchWritePolicy: BatchWritePolicy = null): F[BatchResults]

  def operateBatchRecord(records: Seq[BatchRecord])
                        (implicit policy: BatchPolicy = null): F[Boolean]

  //-------------------------------------------------------
  // Scan Operations
  //-------------------------------------------------------

  def scanNodeName(nodeName: String, ns: String, set: String, binNames: String*)
                  (implicit policy: ScanPolicy = null): F[List[KeyRecord]]

  def scanNode(node: Node, ns: String, set: String, binNames: String*)
              (implicit policy: ScanPolicy = null): F[List[KeyRecord]]

  //----------------------------------------------------------
  // Query/Execute
  //----------------------------------------------------------

  def execute(statement: Statement, operations: Operation*)
             (implicit policy: WritePolicy = null): F[ExecuteTask]

  //--------------------------------------------------------
  // Info
  //--------------------------------------------------------

  def info(node: Node, name: String): F[String]
}
