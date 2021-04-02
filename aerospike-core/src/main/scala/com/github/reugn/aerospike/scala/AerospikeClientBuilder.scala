package com.github.reugn.aerospike.scala

import com.aerospike.client.policy.{AuthMode, ClientPolicy}
import com.aerospike.client.{AerospikeClient, Host}
import com.github.reugn.aerospike.scala.AerospikeClientBuilder._
import com.github.reugn.aerospike.scala.Policies.ClientPolicyImplicits._
import com.typesafe.config.Config

class AerospikeClientBuilder(config: Config) {

  private def buildClientPolicy(): ClientPolicy = {
    val policy = new ClientPolicy();
    Option(config.getString("aerospike.clientpolicy.user")).foreach(policy.user = _)
    Option(config.getString("aerospike.clientpolicy.password")).foreach(policy.password = _)
    Option(config.getString("aerospike.clientpolicy.clusterName")).foreach(policy.clusterName = _)
    Option(config.getString("aerospike.clientpolicy.authMode"))
      .foreach(mode => policy.authMode = AuthMode.valueOf(mode.toUpperCase))
    Option(config.getInt("aerospike.clientpolicy.timeout")).foreach(policy.timeout = _)
    Option(config.getInt("aerospike.clientpolicy.loginTimeout")).foreach(policy.loginTimeout = _)
    Option(config.getInt("aerospike.clientpolicy.minConnsPerNode")).foreach(policy.minConnsPerNode = _)
    Option(config.getInt("aerospike.clientpolicy.maxConnsPerNode")).foreach(policy.maxConnsPerNode = _)
    Option(config.getInt("aerospike.clientpolicy.asyncMinConnsPerNode")).foreach(policy.asyncMinConnsPerNode = _)
    Option(config.getInt("aerospike.clientpolicy.asyncMaxConnsPerNode")).foreach(policy.asyncMaxConnsPerNode = _)
    Option(config.getInt("aerospike.clientpolicy.connPoolsPerNode")).foreach(policy.connPoolsPerNode = _)
    Option(config.getInt("aerospike.clientpolicy.maxSocketIdle")).foreach(policy.maxSocketIdle = _)
    Option(config.getInt("aerospike.clientpolicy.tendInterval")).foreach(policy.tendInterval = _)
    Option(config.getBoolean("aerospike.clientpolicy.failIfNotConnected")).foreach(policy.failIfNotConnected = _)
    Option(config.getBoolean("aerospike.clientpolicy.useServicesAlternate")).foreach(policy.useServicesAlternate = _)
    Option(config.getBoolean("aerospike.clientpolicy.forceSingleNode")).foreach(policy.forceSingleNode = _)
    Option(config.getBoolean("aerospike.clientpolicy.rackAware")).foreach(policy.rackAware = _)
    Option(config.getInt("aerospike.clientpolicy.rackId")).foreach(policy.rackId = _)
    policy.withEventLoops()
  }

  def build(): AerospikeClient = {
    Option(config.getString("aerospike.hostList")) map {
      hostList =>
        new AerospikeClient(buildClientPolicy(), Host.parseHosts(hostList, defaultPort): _*)
    } getOrElse {
      val hostname = Option(config.getString("aerospike.hostname")).getOrElse(defaultHostName)
      val port = Option(config.getInt("aerospike.port")).getOrElse(defaultPort)
      new AerospikeClient(buildClientPolicy(), hostname, port)
    }
  }
}

object AerospikeClientBuilder {
  private[aerospike] val defaultHostName = "localhost"
  private[aerospike] val defaultPort = 3000

  def apply(config: Config): AerospikeClientBuilder = new AerospikeClientBuilder(config)
}
