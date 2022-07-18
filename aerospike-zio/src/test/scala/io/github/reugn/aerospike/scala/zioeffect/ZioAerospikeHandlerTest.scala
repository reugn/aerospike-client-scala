package io.github.reugn.aerospike.scala.zioeffect

import com.aerospike.client._
import com.aerospike.client.exp.{Exp, ExpOperation, ExpReadFlags}
import io.github.reugn.aerospike.scala.TestCommon
import io.github.reugn.aerospike.scala.model.QueryStatement
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.Runtime.{default => rt}

class ZioAerospikeHandlerTest extends AnyFlatSpec with TestCommon with Matchers with BeforeAndAfter {

  private val client: ZioAerospikeHandler = ZioAerospikeHandler(hostname, port)
  override protected val set = "client_zio"

  behavior of "ZioAerospikeHandler"

  before {
    for (t <- populateKeys(client)) {
      rt.unsafeRun(t)
    }
  }

  after {
    for (t <- deleteKeys(client)) {
      rt.unsafeRun(t)
    }
  }

  it should "get record properly" in {
    val t = client.get(keys(0))
    val record = rt.unsafeRun(t)
    record.getLong("intBin") shouldBe 0L
  }

  it should "get records properly" in {
    val t = client.getBatch(keys.toIndexedSeq)
    val records = rt.unsafeRun(t)
    records.size shouldBe keys.length
  }

  it should "get records with read operations properly" in {
    val mulIntBin = "mulIntBin"
    val multiplier = 10L
    val mulExp = Exp.build(Exp.mul(Exp.intBin("intBin"), Exp.`val`(multiplier)))
    val t = client.getBatchOp(keys.toIndexedSeq, ExpOperation.read(mulIntBin, mulExp, ExpReadFlags.DEFAULT))
    val records = rt.unsafeRun(t)
    records.size shouldBe keys.length
    records.zipWithIndex.map { case (rec: Record, i: Int) =>
      val expected = multiplier * i
      rec.getLong(mulIntBin) == expected
    } forall {
      _ == true
    } shouldBe true
  }

  it should "append bin properly" in {
    rt.unsafeRun(client.append(keys(0), new Bin("strBin", "_")))
    val record = rt.unsafeRun(client.get(keys(0)))
    record.getString("strBin") shouldBe "str_0_"
  }

  it should "prepend bin properly" in {
    rt.unsafeRun(client.prepend(keys(0), new Bin("strBin", "_")))
    val record = rt.unsafeRun(client.get(keys(0)))
    record.getString("strBin") shouldBe "_str_0"
  }

  it should "add bin properly" in {
    rt.unsafeRun(client.add(keys(0), new Bin("intBin", 10)))
    val record = rt.unsafeRun(client.get(keys(0)))
    record.getLong("intBin") shouldBe 10L
  }

  it should "delete record properly" in {
    val deleteResult = rt.unsafeRun(client.delete(keys(0)))
    deleteResult shouldBe true
    val record = rt.unsafeRun(client.get(keys(0)))
    record shouldBe null
  }

  it should "delete batch of records properly" in {
    val t = client.deleteBatch(keys.toSeq)
    val result = rt.unsafeRun(t)
    result.status shouldBe true
  }

  it should "record to be exist" in {
    val result = rt.unsafeRun(client.exists(keys(0)))
    result shouldBe true
  }

  it should "records to be exist" in {
    val result = rt.unsafeRun(client.existsBatch(keys.toIndexedSeq))
    result.forall(identity) shouldBe true
  }

  it should "operate bin properly" in {
    rt.unsafeRun(client.operate(keys(0), Operation.put(new Bin("intBin", 100))))
    val record = rt.unsafeRun(client.get(keys(0)))
    record.getLong("intBin") shouldBe 100L
  }

  it should "operate batch of records properly" in {
    val t = client.operateBatch(keys.toSeq,
      Operation.put(new Bin("intBin", 100)))
    val result = rt.unsafeRun(t)
    result.status shouldBe true
  }

  it should "operate list of BatchRecords properly" in {
    val records: Seq[BatchRecord] =
      List(new BatchWrite(keys(0), Array(Operation.put(new Bin("intBin", 100))))) ++
        keys.slice(1, numberOfKeys).map(new BatchDelete(_)).toList
    val t = client.operateBatchRecord(records)
    val result = rt.unsafeRun(t)
    result shouldBe true
  }

  it should "query all properly" in {
    val queryStatement = QueryStatement(namespace, setName = Some(set))
    val t = client.query(queryStatement).runCollect
    rt.unsafeRun(t).length shouldBe numberOfKeys
  }
}
