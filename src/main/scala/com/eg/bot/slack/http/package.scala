package com.eg.bot.slack

import java.util.UUID

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.eg.bot.slack.http.CompanionObject.UrlFormCompanion
import com.eg.bot.slack.http.Exception.HttpException.HeaderNotFound
import com.eg.bot.slack.http.model.{Channel, Text, ThreadTs}
import com.eg.bot.slack.http.route.model.SlackEvent.{EventCallback, UrlVerification}
import com.eg.bot.slack.http.route.model.{Command, SlackEvent}
import com.eg.bot.slack.http.service.model.{RequestEntity, ResponseEntity}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.parser._
import io.circe.{Codec, Decoder, Encoder}
import org.http4s._
import org.http4s.circe.jsonEncoderOf
import org.http4s.util.{CaseInsensitiveString => CIString}
import cats.data.EitherT
import com.eg.bot.slack.http.ShowInstances._

package object http {

  val requestIdHeaderKey: CIString = CIString("X-Request-ID")

  object ShowInstances {

    implicit val textOptShow: Show[Option[Text]] =
      (textOpt: Option[Text]) => s"${textOpt.getOrElse("Text is missing")}"

    implicit val threadTsShow: Show[ThreadTs] =
      (threadTs: ThreadTs) => s"ThreadTs - ${threadTs.value}"

    implicit val headerShow: Show[Header] =
      (header: Header) => s"Header - ${header.name}: ${header.value}"

    implicit val commandShow: Show[Command] =
      (command: Command) => show"Command - type: ${command.`type`.entryName}, text: ${command.text}"

    implicit val eventCallbackShow: Show[EventCallback] =
      (callback: EventCallback) => s"Event Callback - ${callback.event}"

    implicit val postMessageShow: Show[RequestEntity.PostMessage] =
      (postMessage: RequestEntity.PostMessage) =>
        show"PostMessage - text: ${postMessage.text}, channel - ${postMessage.channel.value}, thread_ts - ${postMessage.threadTs}"

  }

  object CompanionObject {

    implicit class MessageCompanion(msg: Message[IO]) {

      val prettyHeaders: String =
        msg.headers
          .redactSensitive(Headers.SensitiveHeaders.contains)
          .toList
          .mkString("Headers(", ", ", ")")

      def getRequestIdHeaderOrEmpty: Header =
        msg.headers.get(requestIdHeaderKey)
          .getOrElse(Header.Raw(requestIdHeaderKey, "UNDEFINED"))

      def getBodyText(): IO[String] =
        msg.bodyText
          .compile
          .string

      def getPrettyBodyText(): IO[String] =
        getBodyText()
          .map(text => s"""body="$text"""")

      def getHeaderEither(headerName: String): Either[HeaderNotFound, Header] =
        msg.headers
          .get(CIString(headerName))
          .toRight(HeaderNotFound(headerName))

    }

    implicit class RequestCompanion(req: Request[IO]) {

      def asAccumulating[A](implicit decoder: Decoder[A]): IO[A] = for {
        bodyText <- req.bodyText.compile.string
        json <- IO.fromEither(
          parse(bodyText)
            .leftMap(parsingFailure => MalformedMessageBodyFailure("Invalid JSON", parsingFailure.some))
        )
        result <- IO.fromEither(
          decoder.decodeAccumulating(json.hcursor)
            .toEither
            .leftMap(nel =>
              InvalidMessageBodyFailure(
                s"Could not decode JSON. Reasons - ${nel.map(_.getMessage()).mkString_(";")}.",
                None
              )
            )
        )
      } yield result

    }

    implicit class UrlFormCompanion(urlForm: UrlForm) {

      def getFirstEither(fieldName: String): Either[InvalidMessageBodyFailure, String] =
        urlForm
          .getFirst(fieldName)
          .toRight(
            InvalidMessageBodyFailure(
              s"The field '$fieldName' is required"
            )
          )

    }

  }

  object Exception {

    abstract class HttpException(val description: String) extends Throwable(description) {

      override def toString: String = description

    }

    object HttpException {

      final case class HeaderNotFound(headerName: String)
        extends HttpException(
          s"The request doesn't contain information about the next header - $headerName."
        )

      final case class IncorrectHeaderValue(header: Header, expectedType: Class[_])
        extends HttpException(
          show"The header contains incorrect value. $header, expectedType - ${expectedType.getName}."
        )

    }

  }

  object Codec {

    implicit val configuration: Configuration =
      Configuration
        .default
        .withSnakeCaseMemberNames
        .withSnakeCaseConstructorNames
    implicit val channelCodec: Codec[Channel] = deriveUnwrappedCodec
    implicit val textCodec: Codec[Text] = deriveUnwrappedCodec
    implicit val threadTsCodec: Codec[ThreadTs] = deriveUnwrappedCodec
    implicit val slackEventDecoder: Decoder[SlackEvent] = {

      val discriminator = "type"
      implicit val configuration: Configuration = Codec.configuration.withDiscriminator(discriminator)
      implicit val tokenDecoder: Decoder[SlackEvent.Token] = deriveUnwrappedDecoder
      implicit val urlVerificationDecoder: Decoder[UrlVerification] = deriveConfiguredDecoder

      implicit val userIdDecoder: Decoder[EventCallback.UserId] = deriveUnwrappedDecoder
      implicit val teamIdDecoder: Decoder[EventCallback.TeamId] = deriveUnwrappedDecoder
      implicit val enterpriseIdDecoder: Decoder[EventCallback.Authorization.EnterpriseId] =
        deriveUnwrappedDecoder
      implicit val authorizationDecoder: Decoder[EventCallback.Authorization] =
        deriveConfiguredDecoder
      implicit val timestampDecoder: Decoder[EventCallback.Event.Timestamp] =
        Decoder.decodeString.emap(
          _.toDoubleOption
            .map(_.toLong)
            .map(EventCallback.Event.Timestamp(_))
            .toRight("Failed to parse passed value")
        )
      implicit val editInformationDecoder: Decoder[EventCallback.Event.Message.EditInformation] =
        deriveConfiguredDecoder
      implicit val botIdDecoder: Decoder[EventCallback.Event.Message.BotProfile.BotId] =
        deriveUnwrappedDecoder
      implicit val botNameDecoder: Decoder[EventCallback.Event.Message.BotProfile.BotName] =
        deriveUnwrappedDecoder
      implicit val appIdDecoder: Decoder[EventCallback.Event.Message.BotProfile.AppId] =
        deriveUnwrappedDecoder
      implicit val botProfileDecoder: Decoder[EventCallback.Event.Message.BotProfile] =
        deriveConfiguredDecoder
      implicit val embeddedRegularMessageDecoder
        : Decoder[EventCallback.Event.Message.EmbeddedMessage.RegularMessage] =
        deriveConfiguredDecoder
      implicit val embeddedMeMessageDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage.MeMessage] =
        deriveConfiguredDecoder
      implicit val embeddedMessageDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage] = {

        val discriminator = "subtype"
        implicit val configuration: Configuration = Codec.configuration.withDiscriminator(discriminator)

        val embeddedRegularMessageWidenDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage] =
          embeddedRegularMessageDecoder.widen
        val embeddedMessageDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage] =
          deriveConfiguredDecoder

        Decoder.decodeJsonObject.flatMap(jsonObj =>
          if (jsonObj.contains(discriminator))
            embeddedMessageDecoder
          else
            embeddedRegularMessageWidenDecoder
        )

      }
      implicit val regularMessageDecoder: Decoder[EventCallback.Event.Message.RegularMessage] =
        deriveConfiguredDecoder
      implicit val changedMessageDecoder: Decoder[EventCallback.Event.Message.MessageChanged] =
        deriveConfiguredDecoder
      implicit val meMessageDecoder: Decoder[EventCallback.Event.Message.MeMessage] =
        deriveConfiguredDecoder
      implicit val deletedMessageDecoder: Decoder[EventCallback.Event.Message.MessageDeleted] =
        deriveConfiguredDecoder
      implicit val messageDecoder: Decoder[EventCallback.Event.Message] = {

        val discriminator = "subtype"
        implicit val configuration: Configuration = Codec.configuration.withDiscriminator(discriminator)

        val regularMessageWidenDecoder: Decoder[EventCallback.Event.Message] = regularMessageDecoder.widen
        val messageDecoder: Decoder[EventCallback.Event.Message] = deriveConfiguredDecoder

        Decoder.decodeJsonObject.flatMap(jsonObj =>
          if (jsonObj.contains(discriminator)) messageDecoder
          else regularMessageWidenDecoder
        )

      }
      implicit val eventDecoder: Decoder[EventCallback.Event] = {

        val eventDecoder: Decoder[EventCallback.Event] = deriveConfiguredDecoder
        val messageWidenDecoder: Decoder[EventCallback.Event] = messageDecoder.widen

        Decoder.decodeJsonObject
          .emap(_(discriminator)
            .toRight(s"The field '$discriminator' is required")
            .flatMap(_.asString
              .toRight(s"The field '$discriminator' must be of the string type"))).flatMap {
            case "message" => messageWidenDecoder
            case _         => eventDecoder
          }

      }
      implicit val eventCallbackDecoder: Decoder[EventCallback] = deriveConfiguredDecoder

      deriveConfiguredDecoder[SlackEvent]

    }

    implicit val commandEntityDecoder: EntityDecoder[IO, Command] = {

      def toCommandType(value: String): Either[InvalidMessageBodyFailure, Command.CommandType] =
        Command.CommandType
          .withNameInsensitiveOption(
            value.replaceAll("[/_]", "")
          ).toRight(
            InvalidMessageBodyFailure(
              s"Unknown command type. Passed value - '$value'"
            )
          )

      EntityDecoder[IO, UrlForm]
        .flatMapR(urlForm =>
          EitherT(
            IO.pure(for {
              commandType <- urlForm
                .getFirstEither("command")
                .flatMap(toCommandType(_))
              text = urlForm
                .getFirst("text")
                .flatMap(text =>
                  if (text.strip().isEmpty) None
                  else text.some
                )
            } yield Command(commandType, text))
          )
        )
    }

    implicit val postMessageEncoder: Encoder[RequestEntity.PostMessage] = deriveConfiguredEncoder
    implicit val postMessageEntityEncoder: EntityEncoder[IO, RequestEntity.PostMessage] = jsonEncoderOf

    implicit val responseEntityDecoder: Decoder[ResponseEntity] =
      Decoder.decodeJsonObject
        .emap(jsonObj =>
          for {
            entity <-
              jsonObj("ok")
                .toRight(s"The field 'ok' is required")
            isSuccess <-
              entity.asBoolean
                .toRight(s"The field 'ok' must be of the boolean type")
          } yield
            if (isSuccess) ResponseEntity.Success
            else ResponseEntity.Failed
        )

  }

  object Generator {

    def generateRequestIdHeader(): IO[Header] =
      IO(UUID.randomUUID().toString())
        .map(Header.Raw(requestIdHeaderKey, _))

  }

}
