package com.eg.bot.slack.service

import cats.effect.IO
import com.eg.bot.slack.TestImplicits
import com.eg.bot.slack.http.model.{Channel, Text}
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.Event.{Message, Timestamp}
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.{TeamId, UserId}
import com.eg.bot.slack.http.service.model.PostMessage
import com.eg.bot.slack.http.service.{EventCallbackHandler, SlackClient}
import com.eg.bot.slack.util.TimeHelper
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec

class EventCallbackHandlerSpec extends AnyFlatSpec {

  "EventCallbackHandler" should "post message in case of RegularMessage | MeMessage" in new Scope {

    eventCallbackHandler.handle(
      Message.RegularMessage(
        Text("test text"),
        UserId("test_user"),
        Timestamp(TimeHelper.getTimestamp),
        TeamId("test_team"),
        Channel("test_channel")
      )
    ).unsafeRunSync()

    eventCallbackHandler.handle(
      Message.MeMessage(
        Text("test text"),
        UserId("test_user"),
        Timestamp(TimeHelper.getTimestamp),
        Channel("test_channel")
      )
    ).unsafeRunSync()

    verify(slackClientMock, times(2))
      .postMessage(*[PostMessage])

  }

  private trait Scope extends TestImplicits with MockitoSugar {

    val slackClientMock = {
      val m = mock[SlackClient]
      when(m.postMessage(any[PostMessage])).thenReturn(IO.unit)
      m
    }
    val eventCallbackHandler: EventCallbackHandler =
      EventCallbackHandler(slackClientMock)

  }

}
