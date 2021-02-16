package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.http.Codec._
import com.eg.bot.slack.http.CompanionObject.RequestCompanion
import com.eg.bot.slack.http.route.model.SlackEvent
import com.eg.bot.slack.http.route.model.SlackEvent.{EventCallback, UrlVerification}
import com.eg.bot.slack.http.service.InteractionQueue
import com.eg.bot.slack.http.CompanionObject._
import com.eg.bot.slack.logging.{Log, LogOf}
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.{Header, HttpRoutes, MediaType, Request, Response}
import cats.syntax.show._
import com.eg.bot.slack.http.ShowInstances._

object EventRoutes extends BaseRoutes {

  def apply(
    interactionQueue: InteractionQueue[EventCallback.Event]
  )(implicit
    logOf: LogOf[IO]
  ): HttpRoutes[IO] = {

    def handler(req: Request[IO])(implicit requestId: Header, logger: Log[IO]): IO[Response[IO]] = (for {
      slackEvent <- req.asAccumulating[SlackEvent]
      response <- handleSlackEvent(slackEvent)
    } yield response)
      .handleErrorWith(routeHandleErrorWith)

    def handleSlackEvent(slackEvent: SlackEvent)(implicit requestId: Header, logger: Log[IO]): IO[Response[IO]] =
      slackEvent match {

        case UrlVerification(_, challenge) =>
          logger.info(show"The url verification request received. $requestId.") *>
            Ok(s"challenge=$challenge")
              .map(_.withContentType(`Content-Type`(MediaType.application.`x-www-form-urlencoded`)))

        case callback: EventCallback => for {
            _ <- logger.info(show"The event callback request received. $callback. $requestId.")
            _ <- interactionQueue.push(callback.event)
            response <- Ok()
          } yield response

      }

    HttpRoutes.of[IO] {

      case req @ POST -> Root => for {
          implicit0(logger: Log[IO]) <- logOf.apply(EventRoutes.getClass)
          implicit0(requestId: Header) = req.getRequestIdHeaderOrEmpty
          response <- handler(req)
        } yield response

    }

  }

}
