package com.eg.bot.slack.http.route

import com.eg.bot.slack.TestImplicits
import org.http4s.{Header, Headers, Method}
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CommandRoutesSpec extends AnyFlatSpec with Matchers {

  "CommandRoutes" should "return 'Received text - test.' for the correct request" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/x-www-form-urlencoded")
          )
        ),
        routes = regularRoutes,
        body = requestBody
      ).unsafeRunSync()

    response.status shouldBe Ok
    response.as[String].unsafeRunSync() shouldBe "Received text - 'test'."
  }

  it should "return 'Received text - Nothing.' for the correct request #1" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/x-www-form-urlencoded")
          )
        ),
        routes = regularRoutes,
        body = "command=%2Ffake_command&text="
      ).unsafeRunSync()

    response.status shouldBe Ok
    response.as[String].unsafeRunSync() shouldBe "Received text - 'Text is missing'."
  }

  it should "return 'Received text - Nothing.' for the correct request #2" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/x-www-form-urlencoded")
          )
        ),
        routes = regularRoutes,
        body = "command=%2Ffake_command&token=04hu6pMLj0lscVDUGuAi6TQK"
      ).unsafeRunSync()

    response.status shouldBe Ok
    response.as[String].unsafeRunSync() shouldBe "Received text - 'Text is missing'."
  }

  it should "return BadRequest for the incorrect request #1" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/x-www-form-urlencoded")
          )
        ),
        routes = regularRoutes,
        body = "command=%2Ffake_command&text=  "
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect request #2" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/x-www-form-urlencoded")
          )
        ),
        routes = regularRoutes,
        body = "fake_key=%2Ffake_command&text=test"
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect request #3" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/json")
          )
        ),
        routes = regularRoutes,
        body = """{"name": "fake_data", "text":"fake_text"}"""
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  private trait Scope extends RoutesBaseScope with TestImplicits {

    val requestBody =
      "token=04hu6pMLj0lscVDUGuAi6TQK&team_id=T01CETVRMDY&team_domain=sbteam-global&channel_id=C01CTH21LV7&channel_name=test&user_id=U01CTGZCX5X&user_name=sergeyqwertyborovskiy&command=%2Ffake_command&text=test&api_app_id=A01JU7WD7B9&is_enterprise_install=false&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT01CETVRMDY%2F1737525197124%2FeonRaBu6pVoPq795l81dm46y&trigger_id=1724583405350.1422947871474.e9f818804a7e4766df5ddb35fb854869"
    val regularRoutes = CommandRoutes()

  }

}
