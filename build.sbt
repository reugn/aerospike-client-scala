val ZIOVersion = "2.0.22"
val MonixVersion = "3.4.1"
val AerospikeVersion = "9.0.2"
val AkkaStreamVersion = "2.8.8"
val NettyVersion = "4.1.116.Final"

lazy val commonSettings = Seq(
  organization := "io.github.reugn",
  scalaVersion := "2.12.20",
  crossScalaVersions := Seq(scalaVersion.value, "2.13.15"),

  libraryDependencies ++= Seq(
    "com.aerospike" % "aerospike-client-jdk8" % AerospikeVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
    "io.netty" % "netty-transport" % NettyVersion,
    "io.netty" % "netty-transport-native-epoll" % NettyVersion classifier "linux-x86_64",
    "io.netty" % "netty-transport-native-kqueue" % NettyVersion classifier "osx-x86_64",
    "io.netty" % "netty-handler" % NettyVersion,
    "com.typesafe" % "config" % "1.4.3",
    "org.scalatest" %% "scalatest" % "3.2.19" % Test
  ),

  scalacOptions := Seq(
    "-target:jvm-1.8",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-encoding", "utf8",
    "-Xlint:-missing-interpolator"
  ),

  Test / parallelExecution := false,
  Test / publishArtifact := false,

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
  publish / skip := true
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
  "dev.zio" %% "zio-interop-reactivestreams" % "2.0.2"
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

lazy val root = (project in file(".")).settings(
  noPublishSettings,
  name := "aerospike-client-scala"
).aggregate(
  core,
  zio,
  monix
)
