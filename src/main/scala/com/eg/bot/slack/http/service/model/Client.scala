package com.eg.bot.slack.http.service.model

import com.eg.bot.slack.http.model.{Channel, Text, ThreadTs}

sealed trait RequestEntity

object RequestEntity {

  final case class PostMessage(
    text: Option[Text],
    channel: Channel,
    threadTs: Option[ThreadTs]
  ) extends RequestEntity

}

sealed trait ResponseEntity

object ResponseEntity {

  final case object Success extends ResponseEntity
  final case object Failed extends ResponseEntity

}
