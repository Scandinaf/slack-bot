package com.eg.bot.slack.config.model

import scala.concurrent.duration.FiniteDuration

final case class HttpServerConfig(
  host: String,
  port: Int,
  idleTimeout: FiniteDuration,
  responseHeaderTimeout: FiniteDuration,
)
