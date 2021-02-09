package com.eg.bot.slack

import cats.kernel.Semigroup
import com.eg.bot.slack.config.model.Secret
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.ProductHint
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader}

package object config {

  object Codec {

    implicit val secretReader =
      ConfigReader[String].map(Secret(_))

  }

  object SemigroupInstances {

    implicit val configReaderFailuresSemigroup = new Semigroup[ConfigReaderFailures] {
      override def combine(x: ConfigReaderFailures, y: ConfigReaderFailures): ConfigReaderFailures =
        x ++ y
    }

  }

  object Hint {

    implicit def camelCaseHint[T]: ProductHint[T] =
      ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))

  }

}
