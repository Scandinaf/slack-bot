package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.http.Codec._
import com.eg.bot.slack.http.route.model.Command
import com.eg.bot.slack.http.CompanionObject._
import com.eg.bot.slack.logging.{Log, LogOf}
import org.http4s.{Header, HttpRoutes, Request, Response}
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import cats.syntax.show._
import com.eg.bot.slack.http.ShowInstances._

object CommandRoutes extends BaseRoutes {

  def apply()(implicit logOf: LogOf[IO]): HttpRoutes[IO] = {

    def handler(req: Request[IO])(implicit requestId: Header, logger: Log[IO]): IO[Response[IO]] = (for {
      command <- req.as[Command]
      _ <- logger.info(show"The following command was received. $command. $requestId.")
      answer = command.text.getOrElse("Text is missing")
      response <- Ok(s"Received text - '$answer'.")
    } yield response)
      .handleErrorWith(routeHandleErrorWith)

    HttpRoutes.of[IO] {

      case req @ POST -> Root => for {
          implicit0(logger: Log[IO]) <- logOf.apply(CommandRoutes.getClass)
          implicit0(requestId: Header) = req.requestIdHeader
          response <- handler(req)
        } yield response

    }

  }

}
