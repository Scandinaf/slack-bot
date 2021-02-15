package com.eg.bot.slack

import cats.kernel.Semigroup
import cats.syntax.either._
import com.eg.bot.slack.config.model.Secret
import org.http4s.Uri
import pureconfig.error.{CannotConvert, ConfigReaderFailures}
import pureconfig.generic.ProductHint
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader}

package object config {

  object Codec {

    implicit val secretReader =
      ConfigReader[String].map(Secret(_))

    implicit val uriReader =
      ConfigReader[String]
        .emap(uri =>
          Uri.fromString(uri)
            .leftMap(pf => CannotConvert(uri, "Uri", pf.message))
        )

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
