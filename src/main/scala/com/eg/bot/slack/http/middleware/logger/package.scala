package com.eg.bot.slack.http.middleware

import cats.Show
import cats.effect.IO
import com.eg.bot.slack.http.CompanionObject.MessageCompanion
import org.http4s.{Request, Response}

package object logger {

  object ShowInstances {

    implicit val requestShow: Show[Request[IO]] =
      (req: Request[IO]) =>
        s"""-------------------->
           |-------------------->
           |-------------------->
           |${req.httpVersion} ${req.method} ${req.uri} ${req.prettyHeaders}"""
          .stripMargin

    implicit val responseShow: Show[Response[IO]] =
      (res: Response[IO]) =>
        s"""<--------------------
           |<--------------------
           |<--------------------
           |${res.httpVersion} ${res.status} ${res.prettyHeaders}"""
          .stripMargin

  }

}
