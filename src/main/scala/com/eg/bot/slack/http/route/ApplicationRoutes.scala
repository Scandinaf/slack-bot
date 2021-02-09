package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.http.middleware.SignedSecretVerifier
import com.eg.bot.slack.logging.LogOf
import org.http4s.HttpRoutes
import org.http4s.server.Router

object ApplicationRoutes {

  def apply(signedSecretVerifier: SignedSecretVerifier)(implicit logOf: LogOf[IO]): HttpRoutes[IO] =
    Router[IO](
    "/slack/command" ->
      signedSecretVerifier.wrap(CommandRoutes())
  )

}
