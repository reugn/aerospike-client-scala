package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.AerospikeException
import com.aerospike.client.listener.InfoListener

import java.util
import scala.collection.JavaConverters._

class ScalaInfoListener extends InfoListener with PromiseLike[Map[String, String]] {

  override def onSuccess(map: util.Map[String, String]): Unit = {
    success(map.asScala.toMap)
  }

  override def onFailure(ae: AerospikeException): Unit = {
    failure(ae)
  }
}
