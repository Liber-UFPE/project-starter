package br.ufpe.liber.controllers

import gg.jte.models.runtime.JteModel
import gg.jte.output.WriterOutput
import io.micronaut.core.io.Writable
import io.micronaut.http.HttpResponse
import java.io.Writer

interface KteController {
    fun ok(model: JteModel): HttpResponse<KteWriteable> = HttpResponse.ok(KteWriteable(model))
    fun notFound(model: JteModel): HttpResponse<KteWriteable> = HttpResponse.notFound(KteWriteable(model))
}

class KteWriteable(private val model: JteModel) : Writable {
    override fun writeTo(out: Writer?) = model.render(WriterOutput(out))
}
