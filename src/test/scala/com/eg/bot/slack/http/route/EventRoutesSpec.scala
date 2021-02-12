package com.eg.bot.slack.http.route

import com.eg.bot.slack.TestImplicits
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.{Header, Headers, MediaType, Method}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EventRoutesSpec extends AnyFlatSpec with Matchers {

  "EventRoutes" should "return passed challenge for the correct url_verification request" in new Scope {
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
        body =
          """{"token": "Jhj5dZrVaK7ZwHHjRyZWjbDl","challenge": "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P","type": "url_verification"}"""
      ).unsafeRunSync()

    response.status shouldBe Ok
    response.contentType shouldBe Some(`Content-Type`(MediaType.application.`x-www-form-urlencoded`))
    response.as[
      String
    ].unsafeRunSync() shouldBe "challenge=3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P"
  }

  it should "return BadRequest for the incorrect url_verification request #1" in new Scope {
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
        body = "token=04hu6pMLj0lscVDUGuAi6TQK&challenge=3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P"
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect url_verification request #2" in new Scope {
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
        body =
          """{"token": "Jhj5dZrVaK7ZwHHjRyZWjbDl","challenge": "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P"}"""
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect url_verification request #3" in new Scope {
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
        body = "{}"
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect url_verification request #4" in new Scope {
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
        body =
          "{token: Jhj5dZrVaK7ZwHHjRyZWjbDl,challenge: 3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P,type: url_verification}"
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect url_verification request #5" in new Scope {
    val response =
      sendRequest(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/json")
          )
        ),
        routes = regularRoutes
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the request with incorrect type" in new Scope {
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
        body =
          """{"token": "Jhj5dZrVaK7ZwHHjRyZWjbDl","challenge": "3eZbrw1aBm2rZgRNFdxV2595E9CY3gmdALWMmHkvFXO7tYXAYM8P","type": "fake_type"}"""
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return Ok for the correct event_callback request" in new Scope {
    val response =
      sendRequestWithBody(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/xml")
          )
        ),
        routes = regularRoutes,
        body =
          """{"token": "Jhj5dZrVaK7ZwHHjRyZWjbDl",
            |"event": {
            |	        "type": "message",
            |	        "channel": "C2147483705",
            |	        "user": "U2147483697",
            |	        "text": "Hello, world!",
            |	        "ts": "1355517523.000005",
            |         "subtype": "bot_message",
            |	        "edited": {
            |		        "user": "U2147483697",
            |		        "ts": "1355517536.000001"
            |	        }
            |},
            |"authorizations": [{
            |         "enterprise_id": "E12345",
            |         "team_id": "T12345",
            |         "user_id": "U12345",
            |         "is_bot": false
            |         }],
            |"type": "event_callback"}""".stripMargin
      ).unsafeRunSync()

    response.status shouldBe Ok
  }

  it should "return BadRequest for the incorrect event_callback request #1" in new Scope {
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
        body =
          """{"token": "Jhj5dZrVaK7ZwHHjRyZWjbDl",
            |"event": {
            |	        "type": "fake_type",
            |	        "channel": "C2147483705",
            |	        "user": "U2147483697",
            |	        "text": "Hello, world!",
            |	        "ts": "1355517523.000005",
            |	        "edited": {
            |		        "user": "U2147483697",
            |		        "ts": "1355517536.000001"
            |	        }
            |},
            |"authorizations": [{
            |         "enterprise_id": "E12345",
            |         "team_id": "T12345",
            |         "user_id": "U12345",
            |         "is_bot": "fake_boolean"
            |         }],
            |"type": "event_callback"}""".stripMargin
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect event_callback request #2" in new Scope {
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
        body =
          """{"token": "Jhj5dZrVaK7ZwHHjRyZWjbDl",
            |"event": {
            |	        "type": "message",
            |	        "channel": "C2147483705",
            |	        "user": "U2147483697",
            |	        "text": "Hello, world!",
            |	        "ts": "fake_ts",
            |         "subtype": "fake_subtype",
            |	        "edited": {
            |		        "user": "U2147483697",
            |		        "ts": "fake_ts"
            |	        }
            |},
            |"authorizations": [{
            |         "enterprise_id": "E12345",
            |         "team_id": "T12345",
            |         "user_id": "U12345",
            |         "is_bot": "fake_boolean"
            |         }],
            |"type": "event_callback"}""".stripMargin
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect event_callback request #3" in new Scope {
    val response =
      sendRequest(
        uri = uri"/",
        method = Method.POST,
        headers = Headers(
          List(
            Header("Content-Type", "application/json")
          )
        ),
        routes = regularRoutes,
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  private trait Scope extends RoutesBaseScope with TestImplicits {

    val regularRoutes = EventRoutes()

  }

}
