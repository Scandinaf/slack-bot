package com.eg.bot.slack.http.service

import cats.effect.IO
import cats.syntax.option._
import com.eg.bot.slack.http.model.Text
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.Event
import com.eg.bot.slack.http.route.model.SlackEvent.EventCallback.Event.Message
import com.eg.bot.slack.http.service.model.RequestEntity
import com.eg.bot.slack.logging.{Log, LogOf}

class EventCallbackHandler(slackClient: SlackClient)(implicit logOf: LogOf[IO])
  extends InteractionQueue.QueueHandler[EventCallback.Event] {

  override def handle(entity: EventCallback.Event): IO[Unit] = for {
    implicit0(logger: Log[IO]) <- logOf.apply(EventCallbackHandler.getClass)
    _ <- entity match {
      case msg: Event.Message => handleMessage(msg)
    }
  } yield ()

  protected def handleMessage(msg: Event.Message)(implicit logger: Log[IO]): IO[Unit] =
    msg match {

      case Message.RegularMessage(text, _, _, _, channel, threadTs) =>
        logger.info("Trying to process an ordinary message.") *>
          slackClient.postMessage(
            RequestEntity.PostMessage(
              text = Text(s"Reply to an ordinary message. Your text - '${text.value}'.").some,
              channel = channel,
              threadTs = threadTs
            )
          )

      case Message.MeMessage(text, _, _, channel, threadTs) =>
        logger.info("Trying to process an me message.") *>
          slackClient.postMessage(
            RequestEntity.PostMessage(
              text = Text(s"Reply to an me message. Your text - ${text.value}.").some,
              channel = channel,
              threadTs
            )
          )

      case _ =>
        logger.warn(s"In current moment we don't have handler for the next type - ${msg.getClass}.")

    }

}

object EventCallbackHandler {

  def apply(slackClient: SlackClient)(implicit logOf: LogOf[IO]): EventCallbackHandler =
    new EventCallbackHandler(slackClient)(logOf)

}
