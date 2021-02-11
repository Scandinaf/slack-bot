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
import org.http4s._
import org.http4s.circe._
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

      implicit val configuration: Configuration =
        Configuration.default
          .withSnakeCaseConstructorNames
          .withSnakeCaseMemberNames
          .withDiscriminator("type")
      implicit val tokenDecoder: Decoder[SlackEvent.Token] = deriveUnwrappedDecoder
      implicit val urlVerificationDecoder: Decoder[UrlVerification] = deriveConfiguredDecoder[UrlVerification]

      implicit val userIdDecoder: Decoder[EventCallback.UserId] = deriveUnwrappedDecoder
      implicit val enterpriseIdDecoder: Decoder[EventCallback.Authorization.EnterpriseId] =
        deriveUnwrappedDecoder
      implicit val teamIdDecoder: Decoder[EventCallback.Authorization.TeamId] = deriveUnwrappedDecoder
      implicit val channelDecoder: Decoder[EventCallback.Event.Channel] = deriveUnwrappedDecoder
      implicit val textDecoder: Decoder[EventCallback.Event.Text] = deriveUnwrappedDecoder
      implicit val timestampDecoder: Decoder[EventCallback.Event.Timestamp] =
        Decoder.decodeString.emap(
          _.toDoubleOption
            .map(_.toLong)
            .map(EventCallback.Event.Timestamp(_))
            .toRight("Failed to parse passed value")
        )

      implicit val subTypeDecoder: Decoder[EventCallback.Event.SubType] =
        deriveEnumerationDecoder[EventCallback.Event.SubType]
      implicit val editInformationDecoder: Decoder[EventCallback.Event.EditInformation] =
        deriveConfiguredDecoder[EventCallback.Event.EditInformation]
      implicit val message: Decoder[EventCallback.Event.Message] =
        deriveConfiguredDecoder[EventCallback.Event.Message]
      implicit val event: Decoder[EventCallback.Event] = deriveConfiguredDecoder[EventCallback.Event]
      implicit val authorizationDecoder: Decoder[EventCallback.Authorization] =
        deriveConfiguredDecoder[EventCallback.Authorization]
      implicit val eventCallbackDecoder: Decoder[EventCallback] = deriveConfiguredDecoder[EventCallback]

      deriveConfiguredDecoder[SlackEvent]

    }

    implicit val slackEventEntityDecoder = jsonOf[IO, SlackEvent]
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
