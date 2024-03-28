package io.github.reugn.aerospike.scala

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.aerospike.client._
import com.aerospike.client.exp.{Exp, ExpOperation, ExpReadFlags}
import com.aerospike.client.policy.BatchWritePolicy
import com.aerospike.client.query.{Filter, KeyRecord}
import io.github.reugn.aerospike.scala.model.QueryStatement
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, FutureOutcome, OptionValues}

import scala.concurrent.{ExecutionContext, Future}

class AerospikeHandlerTest extends AsyncFlatSpec
  with TestCommon with Matchers with BeforeAndAfter with OptionValues {

  private implicit val actorSystem: ActorSystem = ActorSystem("test")
  private implicit val materializer: Materializer = Materializer(actorSystem)

  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val client: AerospikeHandler = AerospikeHandler(hostname, port)

  override def withFixture(test: NoArgAsyncTest) = new FutureOutcome(for {
    _ <- Future.sequence(populateKeys(client))
    result <- super.withFixture(test).toFuture
    _ <- deleteKeys(client)
  } yield result)

  behavior of "AerospikeHandler"

  it should "get record properly" in {
    client.get(keys(0)) map {
      record =>
        record.getLong("intBin") shouldBe 0L
    }
  }

  it should "get records properly" in {
    client.getBatch(keys.toIndexedSeq) map {
      _.size shouldBe keys.length
    }
  }

  it should "get records with read operations properly" in {
    val mulIntBin = "mulIntBin"
    val multiplier = 10L
    val mulExp = Exp.build(Exp.mul(Exp.intBin("intBin"), Exp.`val`(multiplier)))
    client.getBatchOp(keys, ExpOperation.read(mulIntBin, mulExp, ExpReadFlags.DEFAULT)) map { seq =>
      seq.size shouldBe keys.length
      seq.zipWithIndex.map { case (rec: Record, i: Int) =>
        val expected = multiplier * i
        rec.getLong(mulIntBin) == expected
      }
    } map { seq =>
      seq.forall(_ == true)
    } map {
      _ shouldBe true
    }
  }

  it should "append bin properly" in {
    client.append(keys(0), new Bin("strBin", "_")) flatMap {
      _ =>
        client.get(keys(0)) map { record =>
          record.getString("strBin") shouldBe "str_0_"
        }
    }
  }

  it should "prepend bin properly" in {
    client.prepend(keys(0), new Bin("strBin", "_")) flatMap {
      _ =>
        client.get(keys(0)) map { record =>
          record.getString("strBin") shouldBe "_str_0"
        }
    }
  }

  it should "add bin properly" in {
    client.add(keys(0), new Bin("intBin", 10)) flatMap {
      _ =>
        client.get(keys(0)) map { record =>
          record.getLong("intBin") shouldBe 10L
        }
    }
  }

  it should "delete record properly" in {
    client.delete(keys(0)) flatMap {
      result =>
        result shouldBe true
        client.get(keys(0)) map { record =>
          record shouldBe null
        }
    }
  }

  it should "delete batch of records properly" in {
    client.deleteBatch(keys.toSeq) flatMap {
      result =>
        result.status shouldBe true
        client.getBatch(keys.toSeq) map { seq =>
          seq.filter(_ != null)
        } map { record =>
          record shouldBe empty
        }
    }
  }

  it should "record to be exist" in {
    client.exists(keys(0)) map {
      result =>
        result shouldBe true
    }
  }

  it should "records to be exist" in {
    client.existsBatch(keys.toIndexedSeq) map {
      result =>
        result.forall(identity) shouldBe true
    }
  }

  it should "operate bin properly" in {
    client.operate(keys(0), Operation.put(new Bin("intBin", 100))) flatMap {
      _ =>
        client.get(keys(0)) map { record =>
          record.getLong("intBin") shouldBe 100L
        }
    }
  }

  it should "operate batch of records properly" in {
    implicit val bwp: BatchWritePolicy = new BatchWritePolicy
    bwp.expiration = -2
    client.operateBatch(keys.toSeq,
      Operation.put(new Bin("intBin", 100))) flatMap {
      batchResults =>
        batchResults.status shouldBe true
        client.getBatch(keys) map { seq =>
          seq.map { rec =>
            rec.getLong("intBin") == 100L
          }
        } map { seq =>
          seq.forall(_ == true)
        } map {
          _ shouldBe true
        }
    }
  }

  it should "operate list of BatchRecords properly" in {
    val records: Seq[BatchRecord] =
      List(new BatchWrite(keys(0), Array(Operation.put(new Bin("intBin", 100))))) ++
        keys.slice(1, numberOfKeys).map(new BatchDelete(_)).toList
    client.operateBatchRecord(records) map {
      _ shouldBe true
    }
    Thread.sleep(100)
    client.getBatch(keys) map { res =>
      res.filter(_ != null)
    } map { seq =>
      seq.length shouldBe 1
      seq.head.getLong("intBin")
    } map {
      _ shouldBe 100L
    }
  }

  it should "execute info command properly" in {
    val node = client.asJava.getCluster.getRandomNode
    val command = "namespaces"
    client.info(node, command) map { result =>
      result.get(command).value shouldBe namespace
    }
  }

  it should "scan nodes properly" in {
    Future.sequence(client.asJava.getCluster.validateNodes().toList map { node =>
      client.scanNode(node, namespace, set) map {
        _.length
      }
    }).map(_.sum shouldBe numberOfKeys)
  }

  it should "scan nodes by name properly" in {
    Future.sequence(client.asJava.getCluster.validateNodes().toList map { node =>
      client.scanNodeName(node.getName, namespace, set) map {
        _.length
      }
    }).map(_.sum shouldBe numberOfKeys)
  }

  it should "query all properly" in {
    val queryStatement = QueryStatement(namespace, setName = Some(set))
    client.query(queryStatement).runWith(Sink.seq[KeyRecord]) map {
      _.length shouldBe numberOfKeys
    }
  }

  it should "fail on non-existent secondary index query" in {
    val queryStatement = QueryStatement(
      namespace,
      setName = Some(set),
      secondaryIndexFilter = Some(Filter.equal("bin1", 1))
    )
    assertThrows[AerospikeException] {
      client.query(queryStatement).runWith(Sink.seq[KeyRecord])
    }
  }
}
