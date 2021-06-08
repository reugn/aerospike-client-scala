val ZIOVersion = "1.0.3"
val MonixVersion = "3.3.0"
val AerospikeVersion = "5.0.4"
val AkkaStreamVersion = "2.6.12"
val NettyVersion = "4.1.58.Final"

lazy val commonSettings = Seq(
  organization := "io.github.reugn",
  scalaVersion := "2.12.13",
  crossScalaVersions := Seq(scalaVersion.value, "2.13.4"),

  libraryDependencies ++= Seq(
    "com.aerospike" % "aerospike-client" % AerospikeVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaStreamVersion,
    "io.netty" % "netty-all" % NettyVersion,
    "com.typesafe" % "config" % "1.4.1",
    "org.scalatest" %% "scalatest" % "3.2.3" % Test
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

lazy val root = (project in file(".")).settings(
  noPublishSettings,
  name := "aerospike-client-scala"
).aggregate(
  core,
  zio,
  monix
)
