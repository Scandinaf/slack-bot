package com.eg.bot.slack.http.middleware.client

import java.util.UUID

import cats.effect.{IO, Resource}
import org.http4s.Header
import org.http4s.client.Client
import org.http4s.util.{CaseInsensitiveString => CIString}

object RequestId {

  private[this] val headerKey = CIString("X-Request-ID")

  def apply(client: Client[IO]): Client[IO] =
    apply(headerKey)(client)

  def apply(headerKey: CIString)(client: Client[IO]): Client[IO] =
    Client { req =>
      for {
        header <- Resource.liftF(
          req.headers.get(headerKey)
            .map(IO.pure(_))
            .getOrElse(
              IO(UUID.randomUUID().toString())
                .map(
                  Header.Raw(
                    headerKey,
                    _
                  )
                )
            )
        )
        response <- client.run(req.putHeaders(header))
      } yield response.putHeaders(header)
    }

}
