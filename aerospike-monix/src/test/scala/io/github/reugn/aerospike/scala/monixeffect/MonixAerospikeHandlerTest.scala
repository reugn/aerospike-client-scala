package io.github.reugn.aerospike.scala.monixeffect

import com.aerospike.client._
import com.aerospike.client.exp.{Exp, ExpOperation, ExpReadFlags}
import io.github.reugn.aerospike.scala.TestCommon
import io.github.reugn.aerospike.scala.model.QueryStatement
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
    Await.result(deleteKeys(client).runToFuture, Duration.Inf)
  }

  it should "get record properly" in {
    val t = client.get(keys(0))
    val record = Await.result(t.runToFuture, Duration.Inf)
    record.getLong("intBin") shouldBe 0L
  }

  it should "get records properly" in {
    val t = client.getBatch(keys.toIndexedSeq)
    val records = Await.result(t.runToFuture, Duration.Inf)
    records.size shouldBe keys.length
  }

  it should "get records with read operations properly" in {
    val mulIntBin = "mulIntBin"
    val multiplier = 10L
    val mulExp = Exp.build(Exp.mul(Exp.intBin("intBin"), Exp.`val`(multiplier)))
    val t = client.getBatchOp(keys.toIndexedSeq, ExpOperation.read(mulIntBin, mulExp, ExpReadFlags.DEFAULT))
    val records = Await.result(t.runToFuture, Duration.Inf)
    records.size shouldBe keys.length
    records.zipWithIndex.map { case (rec: Record, i: Int) =>
      val expected = multiplier * i
      rec.getLong(mulIntBin) == expected
    } forall {
      _ == true
    } shouldBe true
  }

  it should "append bin properly" in {
    val t = client.append(keys(0), new Bin("strBin", "_"))
    Await.result(t.runToFuture, Duration.Inf)
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record.getString("strBin") shouldBe "str_0_"
  }

  it should "prepend bin properly" in {
    val t = client.prepend(keys(0), new Bin("strBin", "_"))
    Await.result(t.runToFuture, Duration.Inf)
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record.getString("strBin") shouldBe "_str_0"
  }

  it should "add bin properly" in {
    val t = client.add(keys(0), new Bin("intBin", 10))
    Await.result(t.runToFuture, Duration.Inf)
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record.getLong("intBin") shouldBe 10L
  }

  it should "delete record properly" in {
    val deleteResult = Await.result(client.delete(keys(0)).runToFuture, Duration.Inf)
    deleteResult shouldBe true
    val record = Await.result(client.get(keys(0)).runToFuture, Duration.Inf)
    record shouldBe null
  }

  it should "delete batch of records properly" in {
    val t = client.deleteBatch(keys.toSeq)
    val result = Await.result(t.runToFuture, Duration.Inf)
    result.status shouldBe true
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
    record.getLong("intBin") shouldBe 100L
  }

  it should "operate batch of records properly" in {
    val t = client.operateBatch(keys.toSeq,
      Operation.put(new Bin("intBin", 100)))
    val result = Await.result(t.runToFuture, Duration.Inf)
    result.status shouldBe true
  }

  it should "operate list of BatchRecords properly" in {
    val records: Seq[BatchRecord] =
      List(new BatchWrite(keys(0), Array(Operation.put(new Bin("intBin", 100))))) ++
        keys.slice(1, numberOfKeys).map(new BatchDelete(_)).toList
    val t = client.operateBatchRecord(records)
    val result = Await.result(t.runToFuture, Duration.Inf)
    result shouldBe true
  }

  it should "query all properly" in {
    val queryStatement = QueryStatement(namespace, setName = Some(set))
    val observable = client.query(queryStatement)
    val t = observable.consumeWith(Consumer.toList.map(_.length))
    Await.result(t.runToFuture, Duration.Inf) shouldBe numberOfKeys
  }
}
