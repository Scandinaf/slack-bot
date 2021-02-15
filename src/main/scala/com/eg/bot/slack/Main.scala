package com.eg.bot.slack

import java.util.concurrent.Executors

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import com.eg.bot.slack.config.ApplicationConfigReader
import com.eg.bot.slack.http.client.HttpClientModule
import com.eg.bot.slack.http.middleware.SignedSecretVerifier
import com.eg.bot.slack.http.route.ApplicationRoutes
import com.eg.bot.slack.http.server.HttpServerModule
import com.eg.bot.slack.http.service.{EventCallbackHandler, InteractionQueue, SlackClient}
import com.eg.bot.slack.logging.LogOf
import pureconfig.ConfigSource

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = (for {
    implicit0(logOf: LogOf[IO]) <- Resource.liftF(LogOf.slf4j[IO])
    logger <- Resource.liftF(logOf(Main.getClass))

    _ <- Resource.liftF(logger.info("Trying to load application configuration."))
    applicationConfig <- Resource.liftF(
      IO.fromEither(ApplicationConfigReader()
        .readApplicationConfig(ConfigSource.defaultApplication))
    )

    _ <- Resource.liftF(logger.info("Trying to create http client."))
    httpClientBlockingContext = Blocker.liftExecutionContext(
      ExecutionContext.fromExecutorService(
        Executors.newCachedThreadPool()
      )
    )
    httpClient <- HttpClientModule.of(
      applicationConfig.httpClientConfig,
      httpClientBlockingContext
    )
    slackClient = SlackClient(
      httpClient,
      applicationConfig.slackConfig.client
    )
    eventCallbackHandler = EventCallbackHandler(slackClient)
    interactionQueue <- Resource.liftF(
      InteractionQueue.of(
        applicationConfig.slackConfig.queue,
        eventCallbackHandler
      )
    )

    _ <- Resource.liftF(logger.info("Trying to run web-server."))
    signedSecretVerifier = SignedSecretVerifier(
      applicationConfig.slackConfig.server.security.signingSecret
    )
    applicationRoutes = ApplicationRoutes(signedSecretVerifier, interactionQueue)
    httpServerBlockingContext = Blocker.liftExecutionContext(
      ExecutionContext.fromExecutorService(
        Executors.newCachedThreadPool()
      )
    )
    _ <- HttpServerModule.of(
      applicationConfig.httpServerConfig,
      applicationRoutes,
      httpServerBlockingContext
    )
  } yield ())
    .use(_ => IO.never)
    .as(ExitCode.Success)

}
