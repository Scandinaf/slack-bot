package com.eg.bot.slack.http.middleware

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import cats.syntax.either._
import cats.syntax.option._
import com.eg.bot.slack.config.model.Secret
import com.eg.bot.slack.http.CompanionObject._
import com.eg.bot.slack.http.Exception.HttpException
import com.eg.bot.slack.http.Exception.HttpException.IncorrectHeaderValue
import com.eg.bot.slack.http.middleware.Exception.MiddlewareException
import com.eg.bot.slack.http.middleware.Exception.MiddlewareException.{IncorrectSignature, ReplayAttack}
import com.eg.bot.slack.logging.LogOf
import com.eg.bot.slack.util.TimeHelper
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, _}

import scala.util.control.NonFatal

class SignedSecretVerifier(private val signingSecret: Secret)(implicit val logOf: LogOf[IO]) {

  def wrap(routes: HttpRoutes[IO]): HttpRoutes[IO] = {

    def getTimestamp(req: Request[IO]): Either[HttpException, Long] =
      req.getHeader("X-Slack-Request-Timestamp")
        .flatMap(header => {
          Either.catchNonFatal(header.value.toLong)
            .leftMap(_ => IncorrectHeaderValue(header, Long.getClass))
        })

    def checkReplyAttack(timestamp: Long, localTimestamp: Long): Either[MiddlewareException, Unit] =
      if (localTimestamp - timestamp > 300)
        ReplayAttack.asLeft
      else
        ().asRight

    def checkSignature(headerSignature: String, slackSignature: String): Either[MiddlewareException, Unit] =
      if (!headerSignature.equalsIgnoreCase(slackSignature))
        IncorrectSignature.asLeft
      else
        ().asRight

    def generateSlackSignature(sharedSecret: String, text: String): IO[String] = {

      val HexArray = "0123456789ABCDEF".toCharArray

      def toHexString(bytes: Array[Byte]): String = {
        // from https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
        val hexChars = new Array[Char](bytes.length * 2)
        var j = 0
        while (j < bytes.length) {
          val v = bytes(j) & 0xff
          hexChars(j * 2) = HexArray(v >>> 4)
          hexChars(j * 2 + 1) = HexArray(v & 0x0f)
          j += 1
        }
        new String(hexChars)
      }

      val algorithm = "HmacSHA256"
      val secret = new SecretKeySpec(sharedSecret.getBytes, algorithm) //Crypto Funs : 'SHA256' , 'HmacSHA1'

      for {
        mac <- IO(Mac.getInstance(algorithm))
        _ <- IO(mac.init(secret))
        hashString: Array[Byte] <- IO(mac.doFinal(text.getBytes))
      } yield s"v0=${ toHexString(hashString) }"

    }

    Kleisli { req: Request[IO] =>
      OptionT(
        logOf.apply(SignedSecretVerifier.getClass)
          .flatMap(logger => {
            (for {
              _ <- logger.info(s"The following request was received. Request - $req.")
              timestamp <- IO.fromEither(getTimestamp(req))
              _ <- logger.info(s"Attempting to check a request for a reply attack. Request - $req.")
              _ <- IO.fromEither(
                checkReplyAttack(timestamp, TimeHelper.getTimestamp)
              )
              requestBody <- req.bodyText.compile.string
              slackSignature <- generateSlackSignature(signingSecret.value, s"v0:$timestamp:$requestBody")
              headerSignature <- IO.fromEither(req.getHeader("X-Slack-Signature")).map(_.value)
              _ <- logger.info(s"Attempting to check a request signature. Request - $req.")
              _ <- IO.fromEither(
                checkSignature(headerSignature, slackSignature)
              )
            } yield ())
              .as[Option[Response[IO]]](None)
              .handleErrorWith {

                case error: HttpException =>
                  logger.error(
                    s"As part of the request signature verification, there were problems with the received request. Request - $req.",
                    error
                  ) *> BadRequest()
                    .map(_.some)

                case NonFatal(error) =>
                  logger.error(
                    s"As part of the request signature verification, there were problems with the application. Request - $req.",
                    error
                  ) *> InternalServerError()
                    .map(_.some)
              }
          })
      ).orElse(routes(req))

    }

  }

}

object SignedSecretVerifier {

  def apply(signingSecret: Secret)(implicit logOf: LogOf[IO]): SignedSecretVerifier =
    new SignedSecretVerifier(signingSecret)

}
