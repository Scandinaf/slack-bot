package com.eg.bot.slack.http.route

import cats.effect.{Concurrent, IO}
import com.eg.bot.slack.http.middleware.SignedSecretVerifier
import com.eg.bot.slack.logging.LogOf
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import cats.syntax.option._

object ApplicationRoutes {

  def apply(signedSecretVerifier: SignedSecretVerifier)(implicit logOf: LogOf[IO], concurrent: Concurrent[IO]): HttpRoutes[IO] =

    Logger.httpRoutes(
      logHeaders = true,
      logBody = true,
      logAction = (
        (logMsg: String) =>
          logOf.apply(ApplicationRoutes.getClass)
            .flatMap(_.info(logMsg))
        ).some
    )(
      signedSecretVerifier.wrap(
        Router[IO](
          "/slack/command" -> CommandRoutes(),
          "slack/event" -> EventRoutes()
        )
      )
    )

}
