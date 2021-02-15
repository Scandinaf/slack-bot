package com.eg.bot.slack.http.service

import cats.effect.{ContextShift, IO}
import fs2.concurrent.Queue

sealed trait InteractionQueue[E] {

  def push(entity: E): IO[Boolean]

}

object InteractionQueue {

  final case class Config(maxConcurrent: Int)

  trait QueueHandler[E] {

    def handle(entity: E): IO[Unit]

  }

  def of[E](c: Config, h: QueueHandler[E])(implicit cs: ContextShift[IO]): IO[InteractionQueue[E]] =
    for {
      queue <- Queue.unbounded[IO, E]
      _ <- queue.dequeue.mapAsync(c.maxConcurrent)(h.handle(_)).compile.drain.start
    } yield new InteractionQueue[E] {
      override def push(entity: E): IO[Boolean] =
        queue.offer1(entity)
    }

}
