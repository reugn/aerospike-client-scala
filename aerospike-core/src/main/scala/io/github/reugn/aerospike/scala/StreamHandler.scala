package io.github.reugn.aerospike.scala

import com.aerospike.client.policy.QueryPolicy
import io.github.reugn.aerospike.scala.model.QueryStatement

import scala.language.higherKinds

sealed trait StreamHandler

trait StreamHandler1[S[_]] extends StreamHandler {

  def query(statement: QueryStatement)
           (implicit policy: QueryPolicy = null): S[_]
}

trait StreamHandler2[S[_, _]] extends StreamHandler {

  def query(statement: QueryStatement)
           (implicit policy: QueryPolicy = null): S[_, _]
}

trait StreamHandler3[S[_, _, _]] extends StreamHandler {

  def query(statement: QueryStatement)
           (implicit policy: QueryPolicy = null): S[_, _, _]
}
