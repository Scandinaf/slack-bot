package com.eg.bot.slack.service

import cats.effect.IO
import cats.syntax.option._
import com.eg.bot.slack.TestImplicits
import com.eg.bot.slack.http.model.{Channel, Text, ThreadTs}
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.Event.Message.BotProfile
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.Event.{Message, Timestamp}
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.{TeamId, UserId}
import com.eg.bot.slack.http.service.model.RequestEntity
import com.eg.bot.slack.http.service.{EventCallbackHandler, SlackClient}
import com.eg.bot.slack.util.TimeHelper
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec

class EventCallbackHandlerSpec extends AnyFlatSpec {

  "EventCallbackHandler" should "post message in case of RegularMessage | MeMessage" in new Scope {

    (for {
      _ <- eventCallbackHandler.handle(testRegularMessage.copy(botProfile = None))
      _ <- eventCallbackHandler.handle(testRegularMessage)
      _ <- eventCallbackHandler.handle(testMeMessage.copy(botProfile = None))
      _ <- eventCallbackHandler.handle(testMeMessage)
    } yield verify(slackClientMock, times(2))
      .postMessage(*[RequestEntity.PostMessage])).unsafeRunSync()

  }

  private trait Scope extends TestImplicits with MockitoSugar {

    val slackClientMock = {
      val m = mock[SlackClient]
      when(m.postMessage(any[RequestEntity.PostMessage])).thenReturn(IO.unit)
      m
    }
    val eventCallbackHandler: EventCallbackHandler =
      EventCallbackHandler(slackClientMock)

    val testText = Text("test_text")
    val testUserId = UserId("test_user")
    val testTeamId = TeamId("test_team")
    val testTimestamp = Timestamp(TimeHelper.getTimestamp)
    val testChannel = Channel("test_channel")
    val testThreadTs = ThreadTs("test_thread_ts")
    val testBotProfile = BotProfile(
      BotProfile.BotId("test_bot_id"),
      BotProfile.BotName("test_bot_name"),
      BotProfile.AppId("test_app_id"),
      testTeamId
    )
    val testRegularMessage = Message.RegularMessage(
      testText,
      testUserId,
      testTimestamp,
      testTeamId,
      testChannel,
      testThreadTs.some,
      testBotProfile.some
    )
    val testMeMessage = Message.MeMessage(
      testText,
      testUserId,
      testTimestamp,
      testChannel,
      testThreadTs.some,
      testBotProfile.some
    )

  }

}
