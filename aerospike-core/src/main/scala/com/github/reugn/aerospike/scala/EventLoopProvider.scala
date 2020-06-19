package com.github.reugn.aerospike.scala

import com.aerospike.client.async.{EventPolicy, NioEventLoop, NioEventLoops}

object EventLoopProvider {

  lazy val eventLoops: NioEventLoops = new NioEventLoops(new EventPolicy, 1);

  lazy val eventLoop: NioEventLoop = eventLoops.get(0)
}
