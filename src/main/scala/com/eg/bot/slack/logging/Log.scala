package com.eg.bot.slack.logging

import cats.effect.Sync
import org.slf4j.Logger

trait Log[F[_]] {

  def debug(msg: => String): F[Unit]

  def info(msg: => String): F[Unit]

  def warn(msg: => String): F[Unit]

  def warn(msg: => String, cause: Throwable): F[Unit]

  def error(msg: => String): F[Unit]

  def error(msg: => String, cause: Throwable): F[Unit]

}

object Log {

  def apply[F[_] : Sync](logger: Logger): Log[F] =
    new Log[F] {

      def debug(msg: => String) =
        Sync[F].delay {
          if (logger.isDebugEnabled)
            logger.debug(msg)
        }

      def info(msg: => String) =
        Sync[F].delay {
          if (logger.isInfoEnabled)
            logger.info(msg)
        }

      def warn(msg: => String) =
        Sync[F].delay {
          if (logger.isWarnEnabled)
            logger.warn(msg)
        }

      def warn(msg: => String, cause: Throwable) =
        Sync[F].delay {
          if (logger.isWarnEnabled)
            logger.warn(msg, cause)
        }

      def error(msg: => String) =
        Sync[F].delay {
          if (logger.isErrorEnabled)
            logger.error(msg)
        }

      def error(msg: => String, cause: Throwable) =
        Sync[F].delay {
          if (logger.isErrorEnabled)
            logger.error(msg, cause)
        }

    }

}
