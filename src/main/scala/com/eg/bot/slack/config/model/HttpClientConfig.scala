package com.eg.bot.slack.config.model

import scala.concurrent.duration.FiniteDuration

final case class HttpClientConfig(
  connectionTimeout: FiniteDuration,
  requestTimeout: FiniteDuration,
  idleTimeout: FiniteDuration,
)
