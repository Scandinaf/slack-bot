package com.eg.bot.slack.config

import com.eg.bot.slack.config.model.{HttpClientConfig, HttpServerConfig, Secret, SlackConfig}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import com.eg.bot.slack.config.Codec._
import com.eg.bot.slack.config.Hint.camelCaseHint
import com.eg.bot.slack.http.service.InteractionQueue
import org.http4s.Uri

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

  it should "correctly parse configuration for SlackConfig" in new Scope {

    val config =
      """field {
            client {
              baseUri = "https://slack.com/api/"
              security {
                token = "xoxb-1422947871474-1648276662614-oILq1oHCgsRQ6l3lOOAkcfLf"
              }
            }
            server {
              security {
                signingSecret = "2ea6478998957958f8c4fe67d7180b97"
              }
            }
            queue {
              maxConcurrent = 3
            }
          }""".stripMargin
    val slackConfig = read[SlackConfig](config).value

    slackConfig shouldBe a[SlackConfig]
    slackConfig.client shouldBe SlackConfig.Client(
      Uri.unsafeFromString("https://slack.com/api/"),
      SlackConfig.Client.SecurityConfig(
        Secret("xoxb-1422947871474-1648276662614-oILq1oHCgsRQ6l3lOOAkcfLf")
      )
    )
    slackConfig.server shouldBe SlackConfig.Server(
      SlackConfig.Server.SecurityConfig(
        Secret("2ea6478998957958f8c4fe67d7180b97")
      )
    )
    slackConfig.queue shouldBe InteractionQueue.Config(3)

  }

  it should "correctly parse configuration for Uri" in new Scope {
    val config =
      """field = "https://slack.com/api/""""
    val uri = read[Uri](config).value

    uri shouldBe a[Uri]
    uri shouldBe Uri.unsafeFromString("https://slack.com/api/")
  }

  it should "correctly notify about incorrect configuration for Uri" in new Scope {
    val config =
      """field = "fake_protocol://slack.com/api/""""
    val uri = read[Uri](config).left.value

    uri shouldBe a[ConfigReaderFailures]
  }

  private trait Scope {

    def read[T : pureconfig.ConfigReader](
      configString: String,
    ): Either[ConfigReaderFailures, T] =
      ConfigSource
        .string(configString)
        .at("field")
        .load[T]

  }

}
