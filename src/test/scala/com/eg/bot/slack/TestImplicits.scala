package com.eg.bot.slack

import cats.effect.IO
import com.eg.bot.slack.logging.LogOf

trait TestImplicits {

  implicit val logOf: LogOf[IO] =
    LogOf.slf4j[IO].unsafeRunSync()

}
