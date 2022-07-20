package io.github.reugn.aerospike.scala

import com.aerospike.client.{BatchResults, Bin, Key}

import scala.language.higherKinds

trait TestCommon {

  protected val hostname = "localhost"
  protected val port = 3000

  protected val namespace = "test"
  protected val set = "scalaClient"

  protected val numberOfKeys = 10
  protected val keys: Array[Key] = new Array[Key](numberOfKeys)

  protected def populateKeys[T[_]](handler: AsyncHandler[T]): Seq[T[Key]] = {
    (0 until numberOfKeys) map {
      i => {
        val key = new Key(namespace, set, "key_" + i)
        keys(i) = key
        handler.put(key, new Bin("intBin", i), new Bin("strBin", "str_" + i))
      }
    }
  }

  protected def deleteKeys[T[_]](handler: AsyncHandler[T]): T[BatchResults] = {
    handler.deleteBatch(keys)
  }
}
