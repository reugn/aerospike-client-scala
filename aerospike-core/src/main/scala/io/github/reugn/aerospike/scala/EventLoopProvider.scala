package io.github.reugn.aerospike.scala

import com.aerospike.client.async.{EventLoop, EventLoops, EventPolicy, NettyEventLoops}
import io.github.reugn.aerospike.scala.util.{Linux, Mac, OperatingSystem}
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup

object EventLoopProvider {

  private lazy val nThreads: Int = Runtime.getRuntime.availableProcessors

  private[scala] lazy val eventLoops: EventLoops = {
    val eventLoopGroup = OperatingSystem() match {
      case Linux =>
        new EpollEventLoopGroup(nThreads)
      case Mac =>
        new KQueueEventLoopGroup(nThreads)
      case _ =>
        new NioEventLoopGroup(nThreads)
    }
    new NettyEventLoops(new EventPolicy, eventLoopGroup)
  }

  private[scala] def eventLoop: EventLoop = eventLoops.next()
}
