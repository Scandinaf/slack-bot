package com.eg.bot.slack.http.route.middleware

import cats.effect.IO
import com.eg.bot.slack.TestImplicits
import com.eg.bot.slack.config.model.Secret
import com.eg.bot.slack.http.middleware.SignedSecretVerifier
import com.eg.bot.slack.http.route.RoutesBaseScope
import com.eg.bot.slack.util.TimeHelper
import org.http4s.{Header, Headers, HttpRoutes, Method}
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io.->
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SignedSecretVerifierSpec extends AnyFlatSpec with Matchers {

  "SignedSecretVerifier" should "return BadRequest if request doesn't contain the header - 'X-Slack-Request-Timestamp'" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        routes = wrappedRoutes,
        body = requestBody
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest if header contains the incorrect value" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(List(Header("X-Slack-Request-Timestamp", "fake_value"))),
        routes = wrappedRoutes,
        body = requestBody
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest if reply attack check is positive" in new Scope {
    val incorrectTimestamp = (timestamp - 301).toString
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(List(Header("X-Slack-Request-Timestamp", incorrectTimestamp))),
        routes = wrappedRoutes,
        body = requestBody
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest if request doesn't contain the header - 'X-Slack-Signature'" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(List(Header("X-Slack-Request-Timestamp", timestamp.toString))),
        routes = wrappedRoutes,
        body = requestBody
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest if signature check is negative" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("X-Slack-Request-Timestamp", timestamp.toString),
            Header("Content-Type", "application/x-www-form-urlencoded"),
            Header("X-Slack-Signature", "fake_signature")
          )
        ),
        routes = wrappedRoutes,
        body = requestBody
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  private trait Scope extends RoutesBaseScope with TestImplicits {

    private val signedSecret = Secret("2ea6478998957958f8c4fe67d7180b97")
    private val regularRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

      case _ -> Root => Ok()

    }
    private val signedSecretVerifier =
      SignedSecretVerifier(signedSecret)
    val requestBody =
      "token=04hu6pMLj0lscVDUGuAi6TQK&team_id=T01CETVRMDY&team_domain=sbteam-global&channel_id=C01CTH21LV7&channel_name=test&user_id=U01CTGZCX5X&user_name=sergeyqwertyborovskiy&command=%2Ffake_command&text=test&api_app_id=A01JU7WD7B9&is_enterprise_install=false&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT01CETVRMDY%2F1737525197124%2FeonRaBu6pVoPq795l81dm46y&trigger_id=1724583405350.1422947871474.e9f818804a7e4766df5ddb35fb854869"
    val wrappedRoutes = signedSecretVerifier.wrap(regularRoutes)
    val timestamp = TimeHelper.getTimestamp

  }

}
