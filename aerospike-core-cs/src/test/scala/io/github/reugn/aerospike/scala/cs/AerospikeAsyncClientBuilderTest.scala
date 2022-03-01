package io.github.reugn.aerospike.scala.cs

import com.aerospike.client.async.NettyEventLoops
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService, ForAllTestContainer}
import com.typesafe.config.ConfigFactory
import io.github.reugn.aerospike.scala.TestCommon
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import java.io.File

class AerospikeAsyncClientBuilderTest
  extends AsyncFlatSpec
    with TestCommon
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with ForAllTestContainer {

  private val asPort = 3100

  override def beforeAll(): Unit =
    container.start()

  override val container = new DockerComposeContainer(
    new File(getClass.getClassLoader.getResource("docker-compose.yml").toURI),
    Seq(ExposedService("aerospike_1", asPort)),
    env = Map("FEATURE_KEY_FILE" -> "/opt/aerospike/etc/features.conf")
  )

  it should "load config and create event loop" in {
    val resource = getClass.getClassLoader.getResource("reference.conf")
    val myCfg = ConfigFactory.parseFile(new File(resource.toURI))
    val asConf = AerospikeClientConf(myCfg)
    asConf.clientPolicyConf.maxConnsPerNode shouldBe Some(100)
    asConf.clientPolicyConf.eventLoops shouldBe a[NettyEventLoops]
  }
}
