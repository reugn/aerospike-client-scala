package com.github.reugn.aerospike.scala.util

sealed trait OperatingSystem

case object Linux extends OperatingSystem

case object Mac extends OperatingSystem

case object Other extends OperatingSystem

object OperatingSystem {
  def apply(): OperatingSystem = {
    val OSName = System.getProperty("os.name").toLowerCase
    if (OSName.contains("nux")) Linux
    else if (OSName.contains("mac")) Mac
    else Other
  }
}
