package com.lopz.eventviewer.util

/**
 * Los kernel panics (SYSTEM_LAST_KMSG) pueden traer miles de líneas de log.
 * Lo más útil para diagnóstico suele estar en las últimas líneas antes del
 * crash (el "Call trace" o el motivo inmediato), así que mostramos eso
 * en vez del texto completo.
 */
object LogSummarizer {

    private const val MAX_LINES = 60

    fun summarize(rawText: String): String {
        val lines = rawText.lines()
        if (lines.size <= MAX_LINES) return rawText

        val tail = lines.takeLast(MAX_LINES)
        val omitted = lines.size - MAX_LINES
        return "(...se omitieron $omitted líneas iniciales...)\n\n" + tail.joinToString("\n")
    }
}
