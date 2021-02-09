package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.http.Codec._
import com.eg.bot.slack.http.route.model.Command
import com.eg.bot.slack.logging.LogOf
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, MessageFailure}

import scala.util.control.NonFatal

object CommandRoutes {

  def apply()(implicit logOf: LogOf[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root =>
      logOf.apply(CommandRoutes.getClass)
        .flatMap(logger => {

          (for {
            command <- req.as[Command]
            _ <- logger.info(s"The following command was received. $command.")
            answer = command.text.getOrElse("Nothing")
            response <- Ok(s"Received text - $answer.")
          } yield response)
            .handleErrorWith {

              case error: MessageFailure =>
                logger.error(
                  s"As part of the processing of an incoming request there were problems with the received request. Request - $req.",
                  error
                ) *> BadRequest(error.getMessage())

              case NonFatal(error) =>
                logger.error(
                  s"As part of the processing of an incoming request there were problems with the application. Request - $req.",
                  error
                ) *> InternalServerError("Something went wrong, contact the developers.")

            }

        })

  }

}
