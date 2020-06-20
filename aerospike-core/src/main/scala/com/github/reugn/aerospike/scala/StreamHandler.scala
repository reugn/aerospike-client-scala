package com.github.reugn.aerospike.scala

import com.aerospike.client.policy.ScanPolicy
import com.aerospike.client.query.PartitionFilter

import scala.language.higherKinds

sealed trait StreamHandler

trait StreamHandler1[S[_]] extends StreamHandler {

  //-------------------------------------------------------
  // Scan Operations
  //-------------------------------------------------------

  def scanAll(ns: String, set: String, binNames: String*)
             (implicit policy: ScanPolicy = null): S[_]

  def scanPartitions(filter: PartitionFilter, ns: String, set: String, binNames: String*)
                    (implicit policy: ScanPolicy = null): S[_]
}

trait StreamHandler2[S[_, _]] extends StreamHandler {

  //-------------------------------------------------------
  // Scan Operations
  //-------------------------------------------------------

  def scanAll(ns: String, set: String, binNames: String*)
             (implicit policy: ScanPolicy = null): S[_, _]

  def scanPartitions(filter: PartitionFilter, ns: String, set: String, binNames: String*)
                    (implicit policy: ScanPolicy = null): S[_, _]
}

trait StreamHandler3[S[_, _, _]] extends StreamHandler {

  //-------------------------------------------------------
  // Scan Operations
  //-------------------------------------------------------

  def scanAll(ns: String, set: String, binNames: String*)
             (implicit policy: ScanPolicy = null): S[_, _, _]

  def scanPartitions(filter: PartitionFilter, ns: String, set: String, binNames: String*)
                    (implicit policy: ScanPolicy = null): S[_, _, _]
}
