package com.github.reugn.aerospike.scala.monixeffect

import com.aerospike.client.{Bin, Operation}
import com.github.reugn.aerospike.scala.TestCommon
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Consumer
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MonixAerospikeHandlerTest extends AsyncFlatSpec with TestCommon with Matchers with BeforeAndAfter {

  private val client: MonixAerospikeHandler = MonixAerospikeHandler(hostname, port)
  override protected val set = "client_monix"

  behavior of "MonixAerospikeHandler"

  before {
    for (t <- populateKeys(client)) {
      Await.result(t.runToFuture, Duration.Inf)
    }
  }

  after {
    for (t <- deleteKeys(client)) {
      Await.result(t.runToFuture, Duration.Inf)
    }
  }

  it should "get record properly" in {
    val t = client.get(keys(0))
    val record = Await.result(t.runToFuture, Duration.Inf)
    record.bins.get("intBin").asInstanceOf[Long] shouldBe 0
  }

  it should "append bin properly" in {
    val t = client.append(keys(0), new Bin("strBin", "_"))
    Await.result(t.runToFuture, Duration.Inf)
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record.bins.get("strBin").asInstanceOf[String] shouldBe "str_0_"
  }

  it should "prepend bin properly" in {
    val t = client.prepend(keys(0), new Bin("strBin", "_"))
    Await.result(t.runToFuture, Duration.Inf)
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record.bins.get("strBin").asInstanceOf[String] shouldBe "_str_0"
  }

  it should "add bin properly" in {
    val t = client.add(keys(0), new Bin("intBin", 10))
    Await.result(t.runToFuture, Duration.Inf)
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record.bins.get("intBin").asInstanceOf[Long] shouldBe 10
  }

  it should "delete record properly" in {
    val deleteResult = Await.result(client.delete(keys(0)).runToFuture, Duration.Inf)
    deleteResult shouldBe true
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record shouldBe null
  }

  it should "record to be exist" in {
    val result = Await.result(client.exists(keys(0)).runToFuture, Duration.Inf)
    result shouldBe true
  }

  it should "records to be exist" in {
    val result = Await.result(client.existsBatch(keys.toIndexedSeq).runToFuture, Duration.Inf)
    result.forall(identity) shouldBe true
  }

  it should "operate bin properly" in {
    val t = client.operate(keys(0), Operation.put(new Bin("intBin", 100)))
    Await.result(t.runToFuture, Duration.Inf)
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record.bins.get("intBin").asInstanceOf[Long] shouldBe 100
  }

  it should "scan all properly" in {
    val observable = client.scanAll(namespace, set)
    val t = observable.consumeWith(Consumer.toList.map(_.length))
    Await.result(t.runToFuture, Duration.Inf) shouldBe numberOfKeys
  }

}
