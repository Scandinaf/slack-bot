package com.eg.bot.slack

import cats.Show
import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import com.eg.bot.slack.http.Exception.HttpException.HeaderNotFound
import com.eg.bot.slack.http.route.model.Command
import com.eg.bot.slack.http.ShowInstances._
import org.http4s.util.CaseInsensitiveString
import org.http4s._

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

    implicit val commandDecoder: EntityDecoder[IO, Command] =
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
