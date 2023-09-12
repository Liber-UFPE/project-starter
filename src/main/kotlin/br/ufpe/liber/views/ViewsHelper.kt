package br.ufpe.liber.views

import io.micronaut.http.context.ServerRequestContext
import io.micronaut.views.turbo.http.TurboHttpHeaders.TURBO_FRAME
import java.util.Optional

object ViewsHelper {
    fun turboFrame(): Optional<String> = ServerRequestContext.currentRequest<Any>().map { it.headers[TURBO_FRAME] }
    fun isTurboRequest(): Boolean = turboFrame().isPresent
    fun notTurboRequest(): Boolean = turboFrame().isEmpty
}
