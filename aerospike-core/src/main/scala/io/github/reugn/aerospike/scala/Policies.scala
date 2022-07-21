package io.github.reugn.aerospike.scala

import com.aerospike.client.policy._

import scala.language.implicitConversions

object Policies {

  implicit lazy val policy: Policy = new Policy
  implicit lazy val writePolicy: WritePolicy = new WritePolicy
  implicit lazy val queryPolicy: QueryPolicy = new QueryPolicy
  implicit lazy val infoPolicy: InfoPolicy = new InfoPolicy
  implicit lazy val batchPolicy: BatchPolicy = new BatchPolicy

  object ClientPolicyImplicits {

    implicit class Cpi(val clientPolicy: ClientPolicy) {

      implicit def withEventLoops(): ClientPolicy = {
        if (clientPolicy.eventLoops == null)
          clientPolicy.eventLoops = EventLoopProvider.eventLoops
        clientPolicy
      }
    }
  }
}
