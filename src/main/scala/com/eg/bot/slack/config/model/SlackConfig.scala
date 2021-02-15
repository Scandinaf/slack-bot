package com.eg.bot.slack.config.model

import com.eg.bot.slack.config.model.SlackConfig.{Client, Server}
import com.eg.bot.slack.http.service.InteractionQueue
import org.http4s.Uri

final case class SlackConfig(
  client: Client,
  server: Server,
  queue: InteractionQueue.Config
)

object SlackConfig {

  final case class Client(baseUri: Uri, security: Client.SecurityConfig)

  object Client {

    final case class SecurityConfig(token: Secret)

  }

  final case class Server(security: Server.SecurityConfig)

  object Server {

    final case class SecurityConfig(signingSecret: Secret)

  }

}
