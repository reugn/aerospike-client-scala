package io.github.reugn.aerospike.scala.model

import com.aerospike.client.Operation
import com.aerospike.client.query.{Filter, PartitionFilter, Statement}

case class QueryStatement(
                           namespace: String,
                           setName: Option[String] = None,
                           binNames: Option[Seq[String]] = None,
                           secondaryIndexName: Option[String] = None,
                           secondaryIndexFilter: Option[Filter] = None,
                           partitionFilter: Option[PartitionFilter] = None,
                           operations: Option[Seq[Operation]] = None,
                           maxRecords: Option[Long] = None,
                           recordsPerSecond: Option[Int] = None
                         ) {

  lazy val statement: Statement = {
    val statement: Statement = new Statement
    statement.setNamespace(namespace)
    setName.foreach(statement.setSetName)
    binNames.foreach(bins => statement.setBinNames(bins: _*))
    secondaryIndexName.foreach(statement.setIndexName)
    secondaryIndexFilter.foreach(statement.setFilter)
    operations.foreach(ops => statement.setOperations(ops.toArray))
    maxRecords.foreach(statement.setMaxRecords)
    recordsPerSecond.foreach(statement.setRecordsPerSecond)
    statement
  }

  lazy val isScan: Boolean = secondaryIndexName.isEmpty || secondaryIndexFilter.isEmpty
}
