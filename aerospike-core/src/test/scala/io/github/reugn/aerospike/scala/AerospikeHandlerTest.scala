package io.github.reugn.aerospike.scala

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.aerospike.client.query.KeyRecord
import com.aerospike.client.{Bin, Operation}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, FutureOutcome}

import scala.concurrent.{ExecutionContext, Future}

class AerospikeHandlerTest extends AsyncFlatSpec with TestCommon with Matchers with BeforeAndAfter {

  private implicit val actorSystem: ActorSystem = ActorSystem("test")
  private implicit val materializer: Materializer = Materializer(actorSystem)

  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val client: AerospikeHandler = AerospikeHandler(hostname, port)

  override def withFixture(test: NoArgAsyncTest) = new FutureOutcome(for {
    _ <- Future.sequence(populateKeys(client))
    result <- super.withFixture(test).toFuture
    _ <- Future.sequence(deleteKeys(client))
  } yield result)

  behavior of "AerospikeHandler"

  it should "get record properly" in {
    client.get(keys(0)) map {
      record =>
        record.bins.get("intBin").asInstanceOf[Long] shouldBe 0
    }
  }

  it should "append bin properly" in {
    client.append(keys(0), new Bin("strBin", "_")) flatMap {
      _ =>
        client.get(keys(0)) map { record =>
          record.bins.get("strBin").asInstanceOf[String] shouldBe "str_0_"
        }
    }
  }

  it should "prepend bin properly" in {
    client.prepend(keys(0), new Bin("strBin", "_")) flatMap {
      _ =>
        client.get(keys(0)) map { record =>
          record.bins.get("strBin").asInstanceOf[String] shouldBe "_str_0"
        }
    }
  }

  it should "add bin properly" in {
    client.add(keys(0), new Bin("intBin", 10)) flatMap {
      _ =>
        client.get(keys(0)) map { record =>
          record.bins.get("intBin").asInstanceOf[Long] shouldBe 10
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
          record.bins.get("intBin").asInstanceOf[Long] shouldBe 100
        }
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

  it should "scan all properly" in {
    client.scanAll(namespace, set).runWith(Sink.seq[KeyRecord]) map {
      _.length shouldBe numberOfKeys
    }
  }

}
