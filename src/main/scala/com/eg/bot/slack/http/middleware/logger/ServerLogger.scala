package com.eg.bot.slack.http.middleware.logger

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import com.eg.bot.slack.http.middleware.logger.ShowInstances._
import com.eg.bot.slack.logging.LogOf
import org.http4s.{HttpRoutes, Request}

// I know that http4s has its own implementation - import org.http4s.server.middleware.Logger.
// But it don't work correctly.
// logging information about the request body twice.
object ServerLogger extends BaseLogger {

  def apply(routes: HttpRoutes[IO])(implicit logOf: LogOf[IO]): HttpRoutes[IO] =
    Kleisli { req: Request[IO] =>
      OptionT.liftF(
        logOf.apply(ServerLogger.getClass)
          .flatMap(implicit logger => logMessageWithBodyText(req))
      ).flatMap(_ =>
        routes(req).semiflatTap(res => {
          logOf.apply(ServerLogger.getClass)
            .flatMap(implicit logger => logMessageWithBodyText(res))
        })
      )

    }

}
