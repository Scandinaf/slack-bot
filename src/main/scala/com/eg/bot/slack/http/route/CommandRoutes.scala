package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.http.Codec._
import com.eg.bot.slack.http.route.model.Command
import com.eg.bot.slack.logging.LogOf
import org.http4s.HttpRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._

object CommandRoutes extends BaseRoutes {

  def apply()(implicit logOf: LogOf[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ POST -> Root =>
      logOf.apply(CommandRoutes.getClass)
        .flatMap(implicit logger => {

          (for {
            command <- req.as[Command]
            _ <- logger.info(s"The following command was received. $command.")
            answer = command.text.getOrElse("Nothing")
            response <- Ok(s"Received text - $answer.")
          } yield response)
            .handleErrorWith(routeHandleErrorWith(req))

        })

  }

}
