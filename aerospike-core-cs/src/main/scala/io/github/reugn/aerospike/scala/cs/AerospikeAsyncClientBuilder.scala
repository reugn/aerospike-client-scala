package io.github.reugn.aerospike.scala.cs

import com.aerospike.client.{AerospikeClient, Host}
import com.typesafe.config.Config

class AerospikeAsyncClientBuilder(conf: AerospikeClientConf) {
  lazy val eventLoops = conf.getEventLoops()
  val DefaultHostName: String = "localhost"
  val DefaultPort: Int = 3000

  def build(): AerospikeClient = {
    val clientPolicy = conf.clientPolicyConf.getClientPolicy
    val hostName = if (conf.hostname.isDefined) conf.hostname.get else DefaultHostName
    val port = if (conf.port.isDefined) conf.port.get else DefaultPort
    val hosts = if (conf.hosts.isDefined) conf.hosts.get else ""

    if (hosts != "") {
      new AerospikeClient(clientPolicy,
        Host.parseHosts(hosts, port): _*)
    } else {
      new AerospikeClient(clientPolicy, hostName, port)
    }
  }
}

object AerospikeAsyncClientBuilder {
  def apply(config: Config): AerospikeAsyncClientBuilder = new
      AerospikeAsyncClientBuilder(
        AerospikeClientConf(config)
      )

  def apply(aerospikeClientConf: AerospikeClientConf): AerospikeAsyncClientBuilder
  = new AerospikeAsyncClientBuilder(aerospikeClientConf)

  def createClient(aerospikeClientConf: AerospikeClientConf): (AerospikeClient, EventLoops) = {
    val aerospikeClient = (new AerospikeAsyncClientBuilder(aerospikeClientConf)).build()
    (aerospikeClient, aerospikeClientConf.getEventLoops())
  }

  def createClient(config: Config): (AerospikeClient, EventLoops) = {
    val aerospikeClientConf = AerospikeClientConf(config)
    createClient(aerospikeClientConf)
  }
}
