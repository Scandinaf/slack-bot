package com.eg.bot.slack.logging

import cats.effect.Sync
import cats.syntax.functor._
import org.slf4j.{ILoggerFactory, LoggerFactory}

trait LogOf[F[_]] {

  def apply(source: String): F[Log[F]]

  def apply(source: Class[_]): F[Log[F]]

}

object LogOf {

  def apply[F[_] : Sync](factory: ILoggerFactory): LogOf[F] =
    new LogOf[F] {

      def apply(
        source: String
      ): F[Log[F]] =
        for {
          log <-
            Sync[F].delay {
              factory.getLogger(source)
            }
        } yield Log[F](log)

      def apply(source: Class[_]): F[Log[F]] =
        apply(source.getName.stripSuffix("$"))

    }

  def slf4j[F[_] : Sync]: F[LogOf[F]] =
    for {
      factory <- Sync[F].delay { LoggerFactory.getILoggerFactory }
    } yield {
      apply(factory)
    }

}
