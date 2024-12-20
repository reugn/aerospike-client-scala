package io.github.reugn.aerospike.scala.listener

import com.aerospike.client.listener.CommitListener
import com.aerospike.client.{AerospikeException, CommitStatus}

class ScalaCommitListener extends CommitListener with PromiseLike[CommitStatus] {

  override def onSuccess(status: CommitStatus): Unit = {
    success(status)
  }

  override def onFailure(exception: AerospikeException.Commit): Unit = {
    failure(exception)
  }
}
