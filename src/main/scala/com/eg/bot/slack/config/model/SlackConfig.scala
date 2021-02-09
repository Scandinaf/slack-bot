package com.eg.bot.slack.config.model

import com.eg.bot.slack.config.model.SlackConfig.SecurityConfig

final case class SlackConfig(security: SecurityConfig)

object SlackConfig {

  final case class SecurityConfig(signingSecret: Secret)

}
