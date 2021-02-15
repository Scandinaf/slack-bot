package com.eg.bot.slack.http.middleware.logger

import cats.Show
import cats.effect.IO
import cats.syntax.show._
import com.eg.bot.slack.http.CompanionObject.MessageCompanion
import com.eg.bot.slack.logging.Log
import org.http4s.Message

private[middleware] trait BaseLogger {

  protected def logMessageWithBodyText[T <: Message[IO]](
    msg: T
  )(implicit
    logger: Log[IO],
    s: Show[T]
  ): IO[Unit] =
    msg.getPrettyBodyText()
      .flatMap(prettyBodyText => logger.info(show"$msg $prettyBodyText")).as(())

}
