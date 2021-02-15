package com.eg.bot.slack.http.service.model

import com.eg.bot.slack.http.model.{Channel, Text}

final case class PostMessage(
  text: Option[Text],
  channel: Channel
)
