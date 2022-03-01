val ZIOVersion = "1.0.12"
val MonixVersion = "3.4.0"
val AerospikeVersion = "5.1.11"
val AkkaStreamVersion = "2.6.16"
val NettyVersion = "4.1.68.Final"
val NettyIncubatorVersion = "0.0.12.Final"
val akkaVersion = "2.6.18"
val testContainerScalaVersion = "0.39.12"
val pureConfigVersion = "0.17.1"
val scalaLoggingVersion = "3.9.3"

val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion
val pureConfig = "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
val testContainerScala = "com.dimafeng" %% "testcontainers-scala" % testContainerScalaVersion % Test
val testContainerScalaTest = "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainerScalaVersion % Test
val scala212 = "2.12.15"
val scala213 = "2.13.8"

lazy val commonSettings = Seq(
  organization := "io.github.reugn",
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala212,scala213),

  libraryDependencies ++= Seq(
    "com.aerospike" % "aerospike-client" % AerospikeVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
    "io.netty" % "netty-all" % NettyVersion,
    "io.netty.incubator" % "netty-incubator-transport-native-io_uring" % NettyIncubatorVersion,
      pureConfig,
    testContainerScala,
    testContainerScalaTest,
    "org.scalatest" %% "scalatest" % "3.2.10" % Test,
  ),

  scalacOptions := Seq(
    "-target:jvm-1.8",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-encoding", "utf8",
    "-Xlint:-missing-interpolator"
  ),

  parallelExecution in Test := false,
  publishArtifact in Test := false,

  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/reugn/aerospike-client-scala")),
  scmInfo := Some(ScmInfo(url("https://github.com/reugn/aerospike-client-scala"), "git@github.com:reugn/aerospike-client-scala.git")),
  developers := List(Developer("reugn", "reugn", "reugpro@gmail.com", url("https://github.com/reugn"))),
  publishMavenStyle := true,

  pomIncludeRepository := { _ => false },

  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

lazy val core = (project in file("aerospike-core")).settings(
  commonSettings
).settings(
  name := "aerospike-core"
)

lazy val zio = (project in file("aerospike-zio")).settings(
  commonSettings
).settings(
  name := "aerospike-zio"
).settings(libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % ZIOVersion,
  "dev.zio" %% "zio-streams" % ZIOVersion,
  "dev.zio" %% "zio-interop-reactivestreams" % "1.0.3.5"
)).dependsOn(
  core % "test->test;compile->compile"
)

lazy val monix = (project in file("aerospike-monix")).settings(
  commonSettings
).settings(
  name := "aerospike-monix"
).settings(libraryDependencies ++= Seq(
  "io.monix" %% "monix" % MonixVersion
)).dependsOn(
  core % "test->test;compile->compile"
)

lazy val akka = (project in file("aerospike-core-cs")).settings(
  commonSettings
).settings(
  name := "aerospike-core-cs"
).settings(libraryDependencies ++= Seq(
  akkaActor,
  akkaTestkit % "test"
)).dependsOn(
  core % "test->test;compile->compile"
)


lazy val root = (project in file(".")).settings(
  noPublishSettings,
  name := "aerospike-client-scala"
).aggregate(
  core,
  zio,
  monix
)
