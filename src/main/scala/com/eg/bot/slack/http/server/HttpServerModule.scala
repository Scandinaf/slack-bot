package com.eg.bot.slack.http.server

import cats.effect.{Blocker, ContextShift, IO, Resource, Timer}
import com.eg.bot.slack.config.model.HttpServerConfig
import org.http4s.HttpRoutes
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

object HttpServerModule {

  def of(
    config: HttpServerConfig,
    routes: HttpRoutes[IO],
    blocker: Blocker
  )(implicit
    timer: Timer[IO],
    contextShift: ContextShift[IO]
  ): Resource[IO, Server[IO]] =
    BlazeServerBuilder[IO](blocker.blockingContext)
      .withIdleTimeout(config.idleTimeout)
      .withResponseHeaderTimeout(config.responseHeaderTimeout)
      .bindHttp(config.port, config.host)
      .withHttpApp(routes.orNotFound)
      .resource

}
