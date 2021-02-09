package com.eg.bot.slack.config

import com.eg.bot.slack.config.model.{HttpClientConfig, HttpServerConfig, SlackConfig}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import com.eg.bot.slack.config.Codec._
import com.eg.bot.slack.config.Hint.camelCaseHint
import com.eg.bot.slack.config.model.Secret

import scala.concurrent.duration._

class ApplicationConfigReaderSpec extends AnyFlatSpec with Matchers with EitherValues {

  "AppConfig" should "correctly parse configuration for HttpServerConfig" in new Scope {

    val config =
      """{
            field {
              host = "0.0.0.0"
              port = 9091
              idleTimeout = "1m"
              responseHeaderTimeout = "30s"
            }
          }""".stripMargin
    val httpServerConfig = read[HttpServerConfig](config).value

    httpServerConfig shouldBe a[HttpServerConfig]
    httpServerConfig.host shouldBe "0.0.0.0"
    httpServerConfig.port shouldBe 9091
    httpServerConfig.idleTimeout shouldBe 1.minute
    httpServerConfig.responseHeaderTimeout shouldBe 30.second

  }

  it should "correctly parse configuration for HttpClientConfig" in new Scope {

    val config =
      """{
            field {
              requestTimeout = "30s"
              connectionTimeout = "1m"
              idleTimeout = "90s"
            }
          }""".stripMargin
    val httpClientConfig = read[HttpClientConfig](config).value

    httpClientConfig shouldBe a[HttpClientConfig]
    httpClientConfig.requestTimeout shouldBe 30.second
    httpClientConfig.connectionTimeout shouldBe 1.minute
    httpClientConfig.idleTimeout shouldBe 90.second

  }

  it should "correctly parse configuration for SlackConfig.SecurityConfig" in new Scope {

    val config =
      """{
            field {
              signingSecret = "2ea6478998957958f8c4fe67d7180b97"
            }
          }""".stripMargin
    val securityConfig = read[SlackConfig.SecurityConfig](config).value

    securityConfig shouldBe a[SlackConfig.SecurityConfig]
    securityConfig.signingSecret shouldBe Secret("2ea6478998957958f8c4fe67d7180b97")

  }

  private trait Scope {

    def read[T: pureconfig.ConfigReader](
      configString: String,
    ): Either[ConfigReaderFailures, T] =
      ConfigSource
        .string(configString)
        .at("field")
        .load[T]

  }

}
