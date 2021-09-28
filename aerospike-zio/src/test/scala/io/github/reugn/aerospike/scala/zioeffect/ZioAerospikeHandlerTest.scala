package io.github.reugn.aerospike.scala.zioeffect

import com.aerospike.client.{Bin, Operation}
import io.github.reugn.aerospike.scala.TestCommon
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
    record.bins.get("intBin").asInstanceOf[Long] shouldBe 0
  }

  it should "get records properly" in {
    val t = client.getBatch(keys.toIndexedSeq)
    val records = rt.unsafeRun(t)
    records.size shouldBe keys.length
  }

  it should "append bin properly" in {
    rt.unsafeRun(client.append(keys(0), new Bin("strBin", "_")))
    val record = rt.unsafeRun(client.get(keys(0)))
    record.bins.get("strBin").asInstanceOf[String] shouldBe "str_0_"
  }

  it should "prepend bin properly" in {
    rt.unsafeRun(client.prepend(keys(0), new Bin("strBin", "_")))
    val record = rt.unsafeRun(client.get(keys(0)))
    record.bins.get("strBin").asInstanceOf[String] shouldBe "_str_0"
  }

  it should "add bin properly" in {
    rt.unsafeRun(client.add(keys(0), new Bin("intBin", 10)))
    val record = rt.unsafeRun(client.get(keys(0)))
    record.bins.get("intBin").asInstanceOf[Long] shouldBe 10
  }

  it should "delete record properly" in {
    val deleteResult = rt.unsafeRun(client.delete(keys(0)))
    deleteResult shouldBe true
    val record = rt.unsafeRun(client.get(keys(0)))
    record shouldBe null
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
    record.bins.get("intBin").asInstanceOf[Long] shouldBe 100
  }

  it should "scan all properly" in {
    val t = client.scanAll(namespace, set).runCollect
    rt.unsafeRun(t).length shouldBe numberOfKeys
  }

}
