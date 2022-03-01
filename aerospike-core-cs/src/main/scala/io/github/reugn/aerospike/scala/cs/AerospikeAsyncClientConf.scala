package io.github.reugn.aerospike.scala.cs

import com.aerospike.client.async.{EventLoopType, EventLoops, EventPolicy}
import com.aerospike.client.policy.{AuthMode, ClientPolicy}
import com.typesafe.config.Config


final case class EventLoopConf(
                                threadNumbers: Int = 1,
                                eventLoopType: EventLoopType = EventLoopType.DIRECT_NIO,
                                maxCommandsInProcess: Option[Int],
                                maxCommandsInQueue: Option[Int],
                                queueInitialCapacity: Option[Int],
                                minTimeout: Option[Int],
                                ticksPerWheel: Option[Int],
                                commandsPerEventLoop: Option[Int]
                              ) {
  def getEventPolicy: EventPolicy = {
    val policy = new EventPolicy()
    maxCommandsInProcess.foreach(policy.maxCommandsInProcess = _)
    maxCommandsInQueue.foreach(policy.maxCommandsInQueue = _)
    queueInitialCapacity.foreach(policy.queueInitialCapacity = _)
    minTimeout.foreach(policy.minTimeout = _)
    ticksPerWheel.foreach(policy.ticksPerWheel = _)
    commandsPerEventLoop.foreach(policy.commandsPerEventLoop = _)
    policy
  }
}


final case class ClientPolicyConf(
                                   eventLoops: EventLoops,
                                   user: Option[String],
                                   password: Option[String],
                                   clusterName: Option[String],
                                   authMode: Option[String],
                                   timeout: Option[Int],
                                   loginTimeout: Option[Int],
                                   minConnsPerNode: Option[Int],
                                   maxConnsPerNode: Option[Int],
                                   asyncMinConnsPerNode: Option[Int],
                                   asyncMaxConnsPerNode: Option[Int],
                                   connPoolsPerNode: Option[Int],
                                   maxSocketIdle: Option[Int],
                                   maxErrorRate: Option[Int],
                                   errorRateWindow: Option[Int],
                                   tendInterval: Option[Int],
                                   failIfNotConnected: Option[Boolean]
                                 ) {

  def getClientPolicy: ClientPolicy = {
    val policy = new ClientPolicy()
    policy.eventLoops = eventLoops
    user.foreach(policy.user = _)
    password.foreach(policy.password = _)
    clusterName.foreach(policy.clusterName = _)
    authMode.foreach(mode => policy.authMode = AuthMode.valueOf(mode.toUpperCase))
    timeout.foreach(policy.timeout = _)
    loginTimeout.foreach(policy.loginTimeout = _)
    minConnsPerNode.foreach(policy.minConnsPerNode = _)
    maxConnsPerNode.foreach(policy.maxConnsPerNode = _)
    asyncMinConnsPerNode.foreach(policy.asyncMinConnsPerNode = _)
    asyncMaxConnsPerNode.foreach(policy.asyncMaxConnsPerNode = _)
    connPoolsPerNode.foreach(policy.connPoolsPerNode = _)
    maxSocketIdle.foreach(policy.maxSocketIdle = _)
    maxErrorRate.foreach(policy.maxErrorRate = _)
    errorRateWindow.foreach(policy.errorRateWindow = _)
    tendInterval.foreach(policy.tendInterval = _)
    failIfNotConnected.foreach(policy.failIfNotConnected = _)
    policy
  }

}

final case class AerospikeClientConf(
                                      hosts: Option[String] = None,
                                      hostname: Option[String] = None,
                                      port: Option[Int] = None,
                                      clientPolicyConf: ClientPolicyConf
                                    ) {
  def getEventLoops(): EventLoops = clientPolicyConf.eventLoops
}

object AerospikeClientConf {

  def apply(config: Config): AerospikeClientConf = {
    val aerospikeConfig = config.getConfig("aerospike")
    val clientPolicyConfig = aerospikeConfig.getConfig("clientpolicy")
    val eventLoopConfig = clientPolicyConfig.getConfig("eventloop")

    val eventLoopConf = EventLoopConf(
      getIntParam(eventLoopConfig, "threadNumbers").getOrElse(0),
      EventLoopType.valueOf(
        getStringParam(eventLoopConfig, "eventLoopType").getOrElse("DIRECT_NIO")),
      getIntParam(
        eventLoopConfig, "maxCommandsInProcess"
      ),
      getIntParam(
        eventLoopConfig, "maxCommandsInQueue"
      ),
      getIntParam(
        eventLoopConfig, "queueInitialCapacity"
      ),
      getIntParam(
        eventLoopConfig, "minTimeout"
      ),
      getIntParam(
        eventLoopConfig,
        "ticksPerWheel"),
      getIntParam(
        eventLoopConfig,
        "commandsPerEventLoop")
    )

    val clientPolicyConf = ClientPolicyConf(
      EventLoopsProvider.createEventLoops(eventLoopConf),
      getStringParam(clientPolicyConfig,
        "user"),
      getStringParam(clientPolicyConfig, "password"),
      getStringParam(clientPolicyConfig, "clusterName"),
      getStringParam(clientPolicyConfig, "authMode"),
      getIntParam(clientPolicyConfig, "timeout"),
      getIntParam(clientPolicyConfig, "loginTimeout"),
      getIntParam(clientPolicyConfig, "minConnsPerNode"),
      getIntParam(clientPolicyConfig, "maxConnsPerNode"),
      getIntParam(clientPolicyConfig, "asyncMinConnsPerNode"),
      getIntParam(clientPolicyConfig, "asyncMaxConnsPerNode"),
      getIntParam(clientPolicyConfig, "connPoolsPerNode"),
      getIntParam(clientPolicyConfig, "maxSocketIdle"),
      getIntParam(clientPolicyConfig, "maxErrorRate"),
      getIntParam(clientPolicyConfig, "errorRateWindow"),
      getIntParam(clientPolicyConfig, "tendInterval"),
      getBoolean(clientPolicyConfig, "failIfNotConnected")
    )

    AerospikeClientConf(
      getStringParam(aerospikeConfig, "hostList"),
      getStringParam(aerospikeConfig, "hostname"),
      getIntParam(aerospikeConfig, "port"),
      clientPolicyConf
    )
  }

  def getIntParam(config: Config, key: String): Option[Int] = {
    try {
      Option(config.getInt(key))
    } catch {
      case _: Throwable => None
    }
  }

  def getStringParam(config: Config, key: String): Option[String] = {
    try {
      Option(config.getString(key))
    } catch {
      case _: Throwable => None
    }
  }

  def getBoolean(config: Config, key: String): Option[Boolean] = {
    try {
      Option(config.getBoolean(key))
    } catch {
      case _: Throwable => None
    }
  }


}
