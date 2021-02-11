package com.eg.bot.slack.http.route.model

import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.Authorization.{EnterpriseId, TeamId}
import com.eg.bot.slack.http.route.model.SlackEvent.Token
import enumeratum._

sealed trait SlackEvent {

  def token: Token

}

object SlackEvent {

  final case class Token(value: String) extends AnyVal

  final case class UrlVerification(
    token: Token,
    challenge: String
  ) extends SlackEvent

  final case class EventCallback(
    token: Token,
    event: EventCallback.Event,
    authorizations: EventCallback.Authorization
  ) extends SlackEvent

  object EventCallback {

    final case class UserId(value: String) extends AnyVal
    final case class Authorization(
      enterpriseId: EnterpriseId,
      teamId: TeamId,
      userId: UserId,
      isBot: Boolean
    )

    object Authorization {

      final case class EnterpriseId(value: String) extends AnyVal
      final case class TeamId(value: String) extends AnyVal

    }

    sealed trait Event

    object Event {

      final case class Channel(value: String) extends AnyVal
      final case class Text(value: String) extends AnyVal
      final case class Timestamp(value: Long) extends AnyVal
      final case class EditInformation(user: UserId, ts: Timestamp)

      sealed trait SubType extends EnumEntry

      object SubType extends Enum[SubType] {

        val values = findValues

        case object BotMessage extends SubType
        case object MeMessage extends SubType
        case object MessageChanged extends SubType
        case object MessageDeleted extends SubType
        case object MessageReplied extends SubType

      }

      final case class Message(
        channel: Channel,
        user: UserId,
        text: Text,
        ts: Timestamp,
        subtype: Option[SubType],
        edited: Option[EditInformation]
      ) extends EventCallback.Event

    }

  }

}
