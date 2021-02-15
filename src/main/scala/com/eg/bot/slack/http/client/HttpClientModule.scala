package com.eg.bot.slack.http.client

import cats.effect.{Blocker, ConcurrentEffect, IO, Resource}
import com.eg.bot.slack.config.model.HttpClientConfig
import com.eg.bot.slack.logging.LogOf
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import cats.syntax.option._

object HttpClientModule {

  def of(
    config: HttpClientConfig,
    blocker: Blocker
  )(implicit
    ce: ConcurrentEffect[IO],
    logOf: LogOf[IO]
  ): Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](blocker.blockingContext)
      .withConnectTimeout(config.connectionTimeout)
      .withRequestTimeout(config.requestTimeout)
      .withIdleTimeout(config.idleTimeout)
      .resource
      .map(originalClient =>
        Logger(
          logHeaders = true,
          logBody = true,
          logAction = (
            (logMsg: String) =>
              logOf.apply(HttpClientModule.getClass)
                .flatMap(_.info(logMsg))
          ).some
        )(originalClient)
      )

}
