package com.github.reugn.aerospike.scala

import com.aerospike.client.policy.ScanPolicy
import com.aerospike.client.query.PartitionFilter

import scala.language.higherKinds

trait StreamHandler[S[_, _]] {

  //-------------------------------------------------------
  // Scan Operations
  //-------------------------------------------------------

  def scanAll(ns: String, set: String, binNames: String*)
             (implicit policy: ScanPolicy = null): S[_, _]

  def scanPartitions(filter: PartitionFilter, ns: String, set: String, binNames: String*)
                    (implicit policy: ScanPolicy = null): S[_, _]
}
