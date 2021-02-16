package com.eg.bot.slack.http.client

import cats.effect.{Blocker, ConcurrentEffect, IO, Resource}
import com.eg.bot.slack.config.model.HttpClientConfig
import com.eg.bot.slack.http.middleware.client.RequestId
import com.eg.bot.slack.http.middleware.logger.client.Logger
import com.eg.bot.slack.logging.LogOf
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

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
      .map(httpClient =>
        Logger.Response(
          RequestId(
            Logger.Request(
              httpClient
            )
          )
        )
      )

}
