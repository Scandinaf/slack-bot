package com.eg.bot.slack.http.route.model

import enumeratum._

final case class Command(`type`: Command.CommandType, text: Option[String])

object Command {

  sealed trait CommandType extends EnumEntry

  object CommandType extends Enum[CommandType] {

    val values = findValues

    final case object SpecialCommand extends CommandType

  }

}
