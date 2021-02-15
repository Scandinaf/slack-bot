package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.http.Codec._
import com.eg.bot.slack.http.CompanionObject.RequestCompanion
import com.eg.bot.slack.http.route.model.SlackEvent
import com.eg.bot.slack.http.route.model.SlackEvent.{EventCallback, UrlVerification}
import com.eg.bot.slack.http.service.InteractionQueue
import com.eg.bot.slack.logging.{Log, LogOf}
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType, Response}

object EventRoutes extends BaseRoutes {

  def apply(
    interactionQueue: InteractionQueue[EventCallback.Event]
  )(implicit
    logOf: LogOf[IO]
  ): HttpRoutes[IO] = {

    def handleSlackEvent(slackEvent: SlackEvent)(implicit logger: Log[IO]): IO[Response[IO]] =
      slackEvent match {

        case UrlVerification(_, challenge) =>
          logger.info("The url verification request received.") *>
            Ok(s"challenge=$challenge")
              .map(_.withContentType(`Content-Type`(MediaType.application.`x-www-form-urlencoded`)))

        case callback: EventCallback => for {
            _ <- logger.info(s"The event callback request received. Event Callback - $callback.")
            _ <- interactionQueue.push(callback.event)
            response <- Ok()
          } yield response

      }

    HttpRoutes.of[IO] {

      case req @ POST -> Root =>
        logOf.apply(EventRoutes.getClass)
          .flatMap(implicit logger => {

            (for {
              slackEvent <- req.asAccumulating[SlackEvent]
              response <- handleSlackEvent(slackEvent)
            } yield response)
              .handleErrorWith(routeHandleErrorWith(req))

          })

    }

  }

}
