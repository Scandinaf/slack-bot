package com.eg.bot.slack.http.route

import cats.effect.IO
import org.http4s._
import org.http4s.syntax.kleisli._

private[route] trait RoutesBaseScope {

  protected def sendRequest(
    uri: Uri,
    method: Method,
    headers: Headers = Headers.empty,
    routes: HttpRoutes[IO],
  ): IO[Response[IO]] =
    executeRequest(
      routes,
      Request(
        method = method,
        uri = uri,
        headers = headers
      )
    )

  protected def sendRequestWithBody[A](
    uri: Uri,
    method: Method,
    headers: Headers = Headers.empty,
    routes: HttpRoutes[IO],
    body: A
  )(implicit
    encoder: EntityEncoder[IO, A]
  ): IO[Response[IO]] =
    executeRequest(
      routes,
      Request(
        method = method,
        uri = uri,
        headers = headers
      ).withEntity(body)
    )

  private def executeRequest(routes: HttpRoutes[IO], request: Request[IO]) =
    routes
      .orNotFound
      .run(request)

}
