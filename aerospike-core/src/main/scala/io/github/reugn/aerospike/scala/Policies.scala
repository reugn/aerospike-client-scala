package io.github.reugn.aerospike.scala

import com.aerospike.client.policy._

import scala.language.implicitConversions

object Policies {

  implicit lazy val policy: Policy = new Policy
  implicit lazy val writePolicy: WritePolicy = new WritePolicy
  implicit lazy val scanPolicy: ScanPolicy = new ScanPolicy
  implicit lazy val infoPolicy: InfoPolicy = new InfoPolicy

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
