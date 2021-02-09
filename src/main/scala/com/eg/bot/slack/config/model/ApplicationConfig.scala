package com.eg.bot.slack.config.model

final case class ApplicationConfig(
  httpServerConfig: HttpServerConfig,
  httpClientConfig: HttpClientConfig,
  slackConfig: SlackConfig,
)
