package com.eg.bot.slack.http

import com.eg.bot.slack.http.Exception.HttpException

package object middleware {

  object Exception {

    sealed abstract class MiddlewareException(description: String)
      extends HttpException(description)

    object MiddlewareException {

      final case object ReplayAttack
        extends MiddlewareException(
          "The request timestamp is more than five minutes from local time. It could be a replay attack."
        )

      final case object IncorrectSignature extends MiddlewareException("The signatures don't match.")

    }

  }

}
