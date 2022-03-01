package io.github.reugn.aerospike.scala.cs

import com.aerospike.client.async._
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.incubator.channel.uring.IOUringEventLoopGroup

object EventLoopsProvider {
  def createEventLoops(conf: EventLoopConf): EventLoops = {
    val eventPolicy: EventPolicy
    = conf.getEventPolicy
    val threadNums = conf.threadNumbers
    val eventLoopType = conf.eventLoopType

    eventLoopType match {
      case EventLoopType.DIRECT_NIO => new NioEventLoops(eventPolicy, threadNums)
      case EventLoopType.NETTY_NIO =>
        new NettyEventLoops(eventPolicy, new NioEventLoopGroup(threadNums), EventLoopType.NETTY_NIO)
      case EventLoopType.NETTY_EPOLL =>
        new NettyEventLoops(eventPolicy, new EpollEventLoopGroup(threadNums), EventLoopType.NETTY_EPOLL)
      case EventLoopType.NETTY_KQUEUE =>
        new NettyEventLoops(eventPolicy, new KQueueEventLoopGroup(threadNums), EventLoopType.NETTY_EPOLL)
      case EventLoopType.NETTY_IOURING =>
        new NettyEventLoops(eventPolicy,
          new IOUringEventLoopGroup(threadNums), EventLoopType.NETTY_IOURING)
    }
  }
}
