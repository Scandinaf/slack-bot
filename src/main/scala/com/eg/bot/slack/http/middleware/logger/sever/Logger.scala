package com.eg.bot.slack.http.middleware.logger.sever

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import com.eg.bot.slack.http.middleware.logger.BaseLogger
import com.eg.bot.slack.http.middleware.logger.ShowInstances._
import com.eg.bot.slack.logging.LogOf
import org.http4s.HttpRoutes

// I know that http4s has its own implementation - import org.http4s.server.middleware.Logger.
// But it don't work correctly.
// logging information about the request body twice.
object Logger extends BaseLogger {

  object Request {

    def apply(routes: HttpRoutes[IO])(implicit logOf: LogOf[IO]): HttpRoutes[IO] =
      Kleisli { req =>
        OptionT.liftF(
          logOf.apply(Logger.getClass)
            .flatMap(implicit logger => logMessageWithBodyText(req))
        ).flatMap(_ => routes(req))
      }

  }

  object Response {

    def apply(routes: HttpRoutes[IO])(implicit logOf: LogOf[IO]): HttpRoutes[IO] =
      Kleisli { req =>
        routes(req).semiflatTap(res => {
          logOf.apply(Logger.getClass)
            .flatMap(implicit logger => logMessageWithBodyText(res))
        })
      }

  }

}
