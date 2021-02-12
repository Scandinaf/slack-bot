package com.eg.bot.slack

import cats.Show
import cats.effect.IO
import cats.implicits._
import com.eg.bot.slack.http.Exception.HttpException.HeaderNotFound
import com.eg.bot.slack.http.route.model.SlackEvent.{EventCallback, UrlVerification}
import com.eg.bot.slack.http.route.model.{Command, SlackEvent}
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.parser._
import org.http4s._
import org.http4s.util.CaseInsensitiveString
import cats.data.EitherT
import com.eg.bot.slack.http.ShowInstances._

package object http {

  object ShowInstances {

    implicit val headerShow: Show[Header] =
      (header: Header) => s"Header - ${header.name}: ${header.value}"

  }

  object CompanionObject {

    implicit class RequestCompanion(req: Request[IO]) {

      def getHeader(headerName: String): Either[HeaderNotFound, Header] =
        req.headers
          .get(CaseInsensitiveString(headerName))
          .toRight(HeaderNotFound(headerName))

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

    implicit val slackEventDecoder: Decoder[SlackEvent] = {

      val discriminator = "type"
      implicit val configuration: Configuration =
        Configuration.default
          .withSnakeCaseConstructorNames
          .withSnakeCaseMemberNames
          .withDiscriminator(discriminator)
      implicit val tokenDecoder: Decoder[SlackEvent.Token] = deriveUnwrappedDecoder
      implicit val urlVerificationDecoder: Decoder[UrlVerification] = deriveConfiguredDecoder

      implicit val userIdDecoder: Decoder[EventCallback.UserId] = deriveUnwrappedDecoder
      implicit val teamIdDecoder: Decoder[EventCallback.TeamId] = deriveUnwrappedDecoder
      implicit val enterpriseIdDecoder: Decoder[EventCallback.Authorization.EnterpriseId] =
        deriveUnwrappedDecoder
      implicit val authorizationDecoder: Decoder[EventCallback.Authorization] =
        deriveConfiguredDecoder
      implicit val channelDecoder: Decoder[EventCallback.Event.Channel] = deriveUnwrappedDecoder
      implicit val timestampDecoder: Decoder[EventCallback.Event.Timestamp] =
        Decoder.decodeString.emap(
          _.toDoubleOption
            .map(_.toLong)
            .map(EventCallback.Event.Timestamp(_))
            .toRight("Failed to parse passed value")
        )
      implicit val textDecoder: Decoder[EventCallback.Event.Message.Text] = deriveUnwrappedDecoder
      implicit val editInformationDecoder: Decoder[EventCallback.Event.Message.EditInformation] =
        deriveConfiguredDecoder
      implicit val embeddedRegularMessageDecoder
        : Decoder[EventCallback.Event.Message.EmbeddedMessage.RegularMessage] =
        deriveConfiguredDecoder
      implicit val embeddedMeMessageDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage.MeMessage] =
        deriveConfiguredDecoder
      implicit val embeddedMessageDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage] = {

        val discriminator = "subtype"
        implicit val configuration: Configuration =
          Configuration.default
            .withSnakeCaseConstructorNames
            .withSnakeCaseMemberNames
            .withDiscriminator(discriminator)

        val embeddedRegularMessageWidenDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage] =
          embeddedRegularMessageDecoder.widen
        val embeddedMessageDecoder: Decoder[EventCallback.Event.Message.EmbeddedMessage] =
          deriveConfiguredDecoder

        Decoder.decodeJsonObject.flatMap(jsonObj =>
          if (jsonObj.contains(discriminator)) embeddedMessageDecoder
          else embeddedRegularMessageWidenDecoder
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
        implicit val configuration: Configuration =
          Configuration.default
            .withSnakeCaseConstructorNames
            .withSnakeCaseMemberNames
            .withDiscriminator(discriminator)

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

    implicit val commandEntityDecoder: EntityDecoder[IO, Command] =
      EntityDecoder[IO, UrlForm]
        .flatMapR(urlForm =>
          EitherT(
            IO.pure(for {
              command <- urlForm
                .getFirst("command")
                .toRight(
                  InvalidMessageBodyFailure(
                    "The data received doesn't contain any information about the following field - command"
                  )
                )
              text = urlForm
                .getFirst("text")
                .flatMap(text =>
                  if (text.strip().isEmpty) None
                  else text.some
                )
            } yield Command(command, text))
          )
        )

  }

}
