package com.github.reugn.aerospike.scala

import com.aerospike.client.async.{EventLoop, EventLoops, EventPolicy, NettyEventLoops}
import io.netty.channel.nio.NioEventLoopGroup

object EventLoopProvider {

  private lazy val nThreads: Int = Math.ceil(Runtime.getRuntime.availableProcessors / 2.0).toInt

  lazy val eventLoops: EventLoops = new NettyEventLoops(new EventPolicy, new NioEventLoopGroup(nThreads));

  lazy val eventLoop: EventLoop = eventLoops.get(0)
}
