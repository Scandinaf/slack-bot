package com.eg.bot.slack.config

import cats.data.ValidatedNel
import cats.syntax.either._
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderFailures

protected[config] trait ConfigReader {

  protected def read[T : pureconfig.ConfigReader](
    namespace: String
  )(implicit
    source: ConfigObjectSource
  ): Either[ConfigReaderFailures, T] =
    source
      .at(namespace)
      .load[T]

  protected def readValidated[T : pureconfig.ConfigReader](
    namespace: String
  )(implicit
    source: ConfigObjectSource
  ): ValidatedNel[ConfigReaderFailures, T] =
    read(namespace)
      .toValidatedNel

}
