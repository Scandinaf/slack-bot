package com.eg.bot.slack.http.route

import cats.effect.{Concurrent, IO}
import cats.syntax.option._
import com.eg.bot.slack.http.middleware.SignedSecretVerifier
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback
import com.eg.bot.slack.http.service.InteractionQueue
import com.eg.bot.slack.logging.LogOf
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

object ApplicationRoutes {

  def apply(
    signedSecretVerifier: SignedSecretVerifier,
    interactionQueue: InteractionQueue[EventCallback.Event]
  )(implicit
    logOf: LogOf[IO],
    concurrent: Concurrent[IO]
  ): HttpRoutes[IO] =
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
          "slack/event" -> EventRoutes(interactionQueue)
        )
      )
    )

}
