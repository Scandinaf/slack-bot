package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.logging.Log
import org.http4s.dsl.io._
import org.http4s.{Header, MessageFailure, Response}
import cats.syntax.show._
import com.eg.bot.slack.http.ShowInstances._

import scala.util.control.NonFatal

private[route] trait BaseRoutes {

  protected def routeHandleErrorWith(
    implicit
    requestId: Header,
    logger: Log[IO]
  ): Throwable => IO[Response[IO]] = {

    case error: MessageFailure =>
      problemsWithBodyL(error) *> BadRequest()

    case NonFatal(error) =>
      problemsWithApplicationL(error) *> InternalServerError()

  }

  protected def problemsWithBodyL(error: Throwable)(implicit requestId: Header, logger: Log[IO]): IO[Unit] =
    logger.error(
      show"As part of the processing of an incoming request there were problems with the received request. $requestId.",
      error
    )

  protected def problemsWithApplicationL(
    error: Throwable
  )(implicit
    requestId: Header,
    logger: Log[IO]
  ): IO[Unit] =
    logger.error(
      show"As part of the processing of an incoming request there were problems with the application. $requestId.",
      error
    )

}
