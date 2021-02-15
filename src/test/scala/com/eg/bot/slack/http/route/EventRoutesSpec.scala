package com.eg.bot.slack.http.route

import cats.effect.IO
import com.eg.bot.slack.TestImplicits
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback
import com.eg.bot.slack.http.service.InteractionQueue
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.headers.`Content-Type`
import org.http4s.implicits._
import org.http4s.{Header, Headers, MediaType, Method}
import org.mockito.scalatest.MockitoSugar
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

  it should "return Ok for the correct event_callback request #1" in new Scope {
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
            |         "team":"T01CETVRMDY"
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

  it should "return Ok for the correct event_callback request #2" in new Scope {
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
            |         "subtype": "message_changed",
            |	        "channel": "C2147483705",
            |	        "ts": "1355517523.000005",
            |         "previous_message": {
            |         "type":"message",
            |         "text":"Hello World",
            |         "user":"U01CTGZCX5X",
            |         "ts":"1613124306.000600",
            |         "team":"T01CETVRMDY"
            |         },
            |         "message": {
            |         "type":"message",
            |         "text":"Hello World!!!",
            |         "user":"U01CTGZCX5X",
            |         "ts":"1613124306.000600",
            |         "team":"T01CETVRMDY",
            |         "edited": {
            |            "user":"U01CTGZCX5X",
            |            "ts":"1613124345.000000"
            |         }
            |         }
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
            |	        "ts": "1355517523.000005"
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
            |         "subtype": "fake_subtype"
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

  it should "return BadRequest for the incorrect event_callback request #4" in new Scope {
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
            |         "subtype": "message_changed"
            |         },
            |"authorizations": [{
            |         "enterprise_id": "E12345",
            |         "team_id": "T12345",
            |         "user_id": "U12345",
            |         "is_bot": false
            |         }],
            |"type": "event_callback"}""".stripMargin
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  it should "return BadRequest for the incorrect event_callback request #5" in new Scope {
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
            |"type": "fake_type",
            |"channel": "C2147483705",
            |"user": "U2147483697",
            |"text": "Hello, world!",
            |"ts": "1355517523.000005",
            |"team":"T01CETVRMDY"
            |},
            |"authorizations": [{
            |         "enterprise_id": "E12345",
            |         "team_id": "T12345",
            |         "user_id": "U12345",
            |         "is_bot": false
            |         }],
            |"type": "event_callback"}""".stripMargin
      ).unsafeRunSync()

    response.status shouldBe BadRequest
  }

  private trait Scope extends RoutesBaseScope with TestImplicits with MockitoSugar {

    private val interactionQueueMock = {
      val m = mock[InteractionQueue[EventCallback.Event]]
      when(m.push(any[EventCallback.Event]))
        .thenReturn(IO.pure(true))
      m
    }
    val regularRoutes = EventRoutes(interactionQueueMock)

  }

}
