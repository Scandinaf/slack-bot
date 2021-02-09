package com.eg.bot.slack.config

import cats.syntax.contravariantSemigroupal._
import cats.syntax.either._
import com.eg.bot.slack.config.SemigroupInstances._
import com.eg.bot.slack.config.model.{ApplicationConfig, HttpClientConfig, HttpServerConfig}
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._
import com.eg.bot.slack.config.Codec._
import com.eg.bot.slack.config.Hint.camelCaseHint
import com.eg.bot.slack.config.model.SlackConfig
import com.eg.bot.slack.config.model.SlackConfig.SecurityConfig
import pureconfig.ConfigObjectSource

trait ApplicationConfigReader extends ConfigReader {

  type Result = Either[ConfigReaderException[ApplicationConfig], ApplicationConfig]

  def readApplicationConfig(implicit source: ConfigObjectSource): Result

}

object ApplicationConfigReader {

  def apply(): ApplicationConfigReader =
    new ApplicationConfigReader {

      def readSlackConfig(implicit source: ConfigObjectSource) =
        readValidated[SecurityConfig]("slack.security")
          .map(SlackConfig(_))

      override def readApplicationConfig(implicit source: ConfigObjectSource): Result =
        (
          readValidated[HttpServerConfig]("http.server"),
          readValidated[HttpClientConfig]("http.client"),
          readSlackConfig
        )
          .mapN(ApplicationConfig)
          .toEither
          .leftMap(nel => ConfigReaderException(nel.reduce))

    }

}
