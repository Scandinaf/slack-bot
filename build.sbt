import Dependencies._
import sbt.Keys.scalaVersion


name := "slack-bot"
description := "Slack Bot"

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.13.4"
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"))
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full))
  .settings(
    mainClass in(Compile, run) := Some("com.eg.bot.slack.Main"),
    libraryDependencies ++=
      Seq(
        Cats.core,
        Cats.effect,
        FS2.core,
        Http4s.http4sBlazeClient,
        Http4s.http4sBlazeServer,
        Http4s.http4sDsl,
        Http4s.http4sCirce,
        Circe.core,
        Circe.parser,
        Circe.generic,
        Circe.extras,
        Enumeratum.core,
        pureConfig,
        typeSafeConfig,
        scalatest,
        mockito,
        logbackClassic,
        slf4j,
      )
  )