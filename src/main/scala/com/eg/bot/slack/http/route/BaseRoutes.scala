package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.logging.Log
import org.http4s.dsl.io._
import org.http4s.{MessageFailure, Request, Response}

import scala.util.control.NonFatal

private[route] trait BaseRoutes {

  protected def routeHandleErrorWith(request: Request[IO])(implicit logger: Log[IO]): Throwable => IO[Response[IO]] = {

    case error: MessageFailure =>
      problemsWithBodyL(error, request) *> BadRequest()

    case NonFatal(error) =>
      problemsWithApplicationL(error, request) *> InternalServerError()

  }

  protected def problemsWithBodyL(error: Throwable, request: Request[IO])(implicit logger: Log[IO]): IO[Unit] =
    logger.error(
      s"As part of the processing of an incoming request there were problems with the received request. Request - $request.",
      error
    )

  protected def problemsWithApplicationL(error: Throwable, request: Request[IO])(implicit logger: Log[IO]): IO[Unit] =
    logger.error(
      s"As part of the processing of an incoming request there were problems with the application. Request - $request.",
      error
    )

}
