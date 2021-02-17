package com.eg.bot.slack.http.route.model

import com.eg.bot.slack.http.model.{Channel, Text, ThreadTs}

sealed trait SlackEvent {

  def token: SlackEvent.Token

}

object SlackEvent {

  final case class Token(value: String) extends AnyVal

  final case class UrlVerification(
    token: SlackEvent.Token,
    challenge: String
  ) extends SlackEvent

  final case class EventCallback(
    token: SlackEvent.Token,
    event: EventCallback.Event,
    authorizations: List[EventCallback.Authorization]
  ) extends SlackEvent

  object EventCallback {

    final case class UserId(value: String) extends AnyVal
    final case class TeamId(value: String) extends AnyVal

    final case class Authorization(
      enterpriseId: Option[Authorization.EnterpriseId],
      teamId: TeamId,
      userId: UserId,
      isBot: Boolean
    )

    object Authorization {

      final case class EnterpriseId(value: String) extends AnyVal

    }

    sealed trait Event

    object Event {

      final case class Timestamp(value: Long) extends AnyVal

      sealed trait Message extends EventCallback.Event

      object Message {

        final case class EditInformation(user: UserId, ts: Timestamp)
        final case class BotProfile(
          id: BotProfile.BotId,
          name: BotProfile.BotName,
          appId: BotProfile.AppId,
          teamId: TeamId
        )

        object BotProfile {

          final case class BotId(value: String) extends AnyVal
          final case class BotName(value: String) extends AnyVal
          final case class AppId(value: String) extends AnyVal

        }

        final case class RegularMessage(
          text: Text,
          user: UserId,
          ts: Timestamp,
          team: TeamId,
          channel: Channel,
          threadTs: Option[ThreadTs],
          botProfile: Option[BotProfile]
        ) extends Message

        final case class MessageChanged(
          channel: Channel,
          ts: Timestamp,
          previousMessage: EmbeddedMessage,
          message: EmbeddedMessage
        ) extends Message

        final case class MeMessage(
          text: Text,
          user: UserId,
          ts: Timestamp,
          channel: Channel,
          threadTs: Option[ThreadTs],
          botProfile: Option[BotProfile]
        ) extends Message

        final case class MessageDeleted(
          ts: Timestamp,
          channel: Channel,
          previousMessage: EmbeddedMessage,
        ) extends Message

        sealed trait EmbeddedMessage

        object EmbeddedMessage {

          final case class RegularMessage(
            text: Text,
            user: UserId,
            ts: Timestamp,
            team: TeamId,
            edited: Option[EditInformation],
            threadTs: Option[ThreadTs],
            botProfile: Option[BotProfile]
          ) extends EmbeddedMessage

          final case class MeMessage(
            text: Text,
            user: UserId,
            ts: Timestamp,
            edited: Option[EditInformation],
            threadTs: Option[ThreadTs],
            botProfile: Option[BotProfile]
          ) extends EmbeddedMessage

        }

      }

    }

  }

}
