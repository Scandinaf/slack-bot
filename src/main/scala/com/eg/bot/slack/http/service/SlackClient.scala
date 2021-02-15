package com.eg.bot.slack.http.service

import cats.effect.IO
import com.eg.bot.slack.config.model.SlackConfig
import com.eg.bot.slack.http.service.model.PostMessage
import com.eg.bot.slack.logging.{Log, LogOf}
import org.http4s.{Header, Headers, Method, Request, Response}
import org.http4s.client.Client
import com.eg.bot.slack.http.Codec._

class SlackClient(httpClient: Client[IO], config: SlackConfig.Client)(implicit logOf: LogOf[IO]) {

  protected val baseHeaders: Headers =
    Headers(
      List(
        Header("Content-Type", "application/json"),
        Header("Authorization", s"Bearer ${config.security.token.value}"),
      )
    )

  def postMessage(entity: PostMessage): IO[Unit] = for {
    implicit0(logger: Log[IO]) <- logOf.apply(SlackClient.getClass)
    _ <- logger.info(s"Trying to post message. PostMessage - $entity.")
    request = Request[IO](
      method = Method.POST,
      uri = config.baseUri.addPath("chat.postMessage"),
      headers = baseHeaders
    ).withEntity(entity)
    _ <- httpClient
      .run(request)
      .use(handleResponse(_))
  } yield ()

  protected def handleResponse(response: Response[IO])(implicit logger: Log[IO]): IO[Unit] =
    IO.whenA(response.status.code != 200)(logger.error(s"The request failed. Response - $response."))

}

object SlackClient {

  def apply(httpClient: Client[IO], config: SlackConfig.Client)(implicit logOf: LogOf[IO]): SlackClient =
    new SlackClient(httpClient, config)

}
