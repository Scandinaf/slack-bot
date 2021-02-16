package com.eg.bot.slack.http.middleware.logger.client

import cats.effect.{IO, Resource}
import cats.syntax.apply._
import com.eg.bot.slack.http.middleware.logger.BaseLogger
import com.eg.bot.slack.http.middleware.logger.ShowInstances._
import com.eg.bot.slack.logging.LogOf
import org.http4s.client.Client

// I know that http4s has its own implementation - import org.http4s.client.middleware.Logger.
// But it don't work correctly.
object Logger extends BaseLogger {

  object Request {

    def apply(client: Client[IO])(implicit logOf: LogOf[IO]): Client[IO] =
      Client { req =>
        Resource.liftF(
          logOf.apply(Logger.getClass)
            .flatMap(implicit logger => logMessageWithBodyText(req))
        ) *> client.run(req)
      }

  }

  object Response {

    def apply(client: Client[IO])(implicit logOf: LogOf[IO]): Client[IO] =
      Client { req =>
        client.run(req).flatMap(res =>
          Resource.liftF({
            logOf.apply(Logger.getClass)
              .flatMap(implicit logger => logMessageWithBodyText(res))
          }.as(res))
        )
      }

  }

}
