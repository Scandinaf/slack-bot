import sbt._

object Dependencies {

  object FS2 {

    private val version = "2.5.0"

    val core = "co.fs2" %% "fs2-core" % version

  }

  object Cats {

    private val version = "2.3.1"

    val core = "org.typelevel" %% "cats-core" % version
    val effect = "org.typelevel" %% "cats-effect" % version

  }

  object Http4s {

    private val version = "0.21.15"

    val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client" % version
    val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % version
    val http4sDsl = "org.http4s" %% "http4s-dsl" % version
    val http4sCirce = "org.http4s" %% "http4s-circe" % version

  }

  object Circe {

    private val version = "0.13.0"

    val core = "io.circe" %% "circe-core" % version
    val generic = "io.circe" %% "circe-generic" % version
    val parser = "io.circe" %% "circe-parser" % version
    val extras = "io.circe" %% "circe-generic-extras" % version

  }

  object Enumeratum {

    private val version = "1.6.1"

    val core = "com.beachape" %% "enumeratum" % version

  }

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.14.0"
  val typeSafeConfig = "com.typesafe" % "config" % "1.4.1"

  val slf4j = "org.slf4j" % "slf4j-api" % "2.0.0-alpha1"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.3.0-alpha5"

  val scalatest = "org.scalatest" %% "scalatest" % "3.3.0-SNAP3" % Test
  val mockito = "org.mockito" %% "mockito-scala-scalatest" % "1.16.3" % Test

}