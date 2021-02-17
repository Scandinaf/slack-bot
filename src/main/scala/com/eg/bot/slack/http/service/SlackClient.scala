package com.eg.bot.slack.http.service

import cats.effect.IO
import com.eg.bot.slack.config.model.SlackConfig
import com.eg.bot.slack.http.service.model.{RequestEntity, ResponseEntity}
import com.eg.bot.slack.logging.{Log, LogOf}
import org.http4s.{Header, Headers, Method, Request, Response}
import org.http4s.client.Client
import com.eg.bot.slack.http.Codec._
import com.eg.bot.slack.http.Generator
import cats.syntax.show._
import org.http4s.circe._
import com.eg.bot.slack.http.ShowInstances._

import scala.util.control.NonFatal

class SlackClient(httpClient: Client[IO], config: SlackConfig.Client)(implicit logOf: LogOf[IO]) {

  def postMessage(entity: RequestEntity.PostMessage): IO[Unit] = for {
    implicit0(logger: Log[IO]) <- logOf.apply(SlackClient.getClass)
    implicit0(requestId: Header) <- Generator.generateRequestIdHeader()
    _ <- logger.info(show"Trying to post message. $entity. $requestId.")
    request = Request[IO](
      method = Method.POST,
      uri = config.baseUri.addPath("chat.postMessage"),
      headers = buildBaseHeaders(requestId)
    ).withEntity(entity)
    _ <- httpClient
      .run(request)
      .use(handleResponse(_))
  } yield ()

  protected def handleResponse(
    response: Response[IO]
  )(implicit
    requestId: Header,
    logger: Log[IO]
  ): IO[Unit] =
    if (response.status.code != 200)
      logger.error(
        show"The status of the response received isn't as expected. Status code - ${response.status.code}. $requestId."
      )
    else
      (for {
        responseEntity <- response.decodeJson[ResponseEntity]
        _ <- responseEntity match {
          case ResponseEntity.Success =>
            logger.info(show"The request sent was successful. $requestId.")
          case ResponseEntity.Failed =>
            logger.error(show"There is a problem with the sent request. $requestId.")
        }
      } yield ()).handleErrorWith {

        case NonFatal(error) =>
          logger.error(
            show"In the process of decoding the received entity there were problems. $requestId.",
            error
          )

      }

  protected def buildBaseHeaders(requestId: Header): Headers =
    Headers(
      List(
        Header("Content-Type", "application/json"),
        Header("Authorization", s"Bearer ${config.security.token.value}"),
        requestId
      )
    )

}

object SlackClient {

  def apply(httpClient: Client[IO], config: SlackConfig.Client)(implicit logOf: LogOf[IO]): SlackClient =
    new SlackClient(httpClient, config)

}
