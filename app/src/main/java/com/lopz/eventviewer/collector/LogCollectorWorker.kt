package com.lopz.eventviewer.collector

import android.content.Context
import android.os.DropBoxManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lopz.eventviewer.data.AppDatabase
import com.lopz.eventviewer.data.EventCategory
import com.lopz.eventviewer.data.EventSeverity
import com.lopz.eventviewer.data.SystemEvent

/**
 * Recolecta eventos de dos fuentes:
 * 1. DropBoxManager: la fuente "oficial" de Android para crashes, ANRs,
 *    kernel panics y tombstones. Requiere permiso READ_LOGS.
 * 2. logcat: buffer completo del sistema, útil como contexto adicional.
 *
 * Las tags de DropBox que nos interesan (son constantes conocidas del sistema,
 * no todas están expuestas como constantes públicas en el SDK):
 * - "SYSTEM_LAST_KMSG"      -> kernel panic / reinicio forzado
 * - "SYSTEM_TOMBSTONE"      -> crash nativo (C/C++)
 * - "system_app_crash"      -> crash de una app (Java/Kotlin)
 * - "system_app_anr"        -> ANR (app no responde)
 * - "system_app_wtf"        -> error interno grave reportado por el propio sistema
 * - "SYSTEM_RECOVERY_LOG"   -> log de arranque en modo recovery
 */
class LogCollectorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private val DROPBOX_TAGS = listOf(
            "SYSTEM_LAST_KMSG",
            "SYSTEM_TOMBSTONE",
            "system_app_crash",
            "system_app_anr",
            "system_app_wtf",
            "SYSTEM_RECOVERY_LOG"
        )
    }

    override suspend fun doWork(): Result {
        return try {
            val dao = AppDatabase.getInstance(applicationContext).systemEventDao()
            val events = mutableListOf<SystemEvent>()

            events.addAll(collectFromDropBox())
            events.addAll(collectFromLogcat())

            // Evitar duplicados: solo insertamos lo que no existe ya
            val newEvents = events.filter { dao.exists(it.timestamp, it.rawDetails) == 0 }
            dao.insertAll(newEvents)

            Result.success()
        } catch (e: SecurityException) {
            android.util.Log.e("EventViewer", "SecurityException en recolección", e)
            Result.failure()
        } catch (e: Exception) {
            android.util.Log.e("EventViewer", "Error en recolección", e)
            Result.retry()
        }
    }

    private fun collectFromDropBox(): List<SystemEvent> {
        val dropBox = applicationContext.getSystemService(Context.DROPBOX_SERVICE) as? DropBoxManager
            ?: return emptyList()

        val results = mutableListOf<SystemEvent>()

        for (tag in DROPBOX_TAGS) {
            // Empezamos desde hace 7 días; se puede ajustar o guardar el último timestamp leído
            var msec = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

            while (true) {
                val entry = dropBox.getNextEntry(tag, msec) ?: break
                // getText requiere el máximo de bytes a leer; 500_000 alcanza de sobra para un log de crash/panic
                val text = entry.getText(500_000) ?: "(sin contenido de texto, posible entrada binaria)"
                val timestamp = entry.timeMillis

                results.add(classifyEntry(tag, text, timestamp))
                msec = entry.timeMillis
                entry.close()
            }
        }

        return results
    }

    private fun collectFromLogcat(): List<SystemEvent> {
        val results = mutableListOf<SystemEvent>()
        try {
            // -v time agrega fecha/hora al inicio de cada línea (ej: "07-12 15:23:54.896 ...")
            // -d = dump y salir (no queda escuchando). -b all = todos los buffers disponibles.
            val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "time", "-b", "all"))
            val lines = process.inputStream.bufferedReader().readLines()

            val oomLines = lines.filter {
                it.contains("Low on memory", ignoreCase = true) ||
                        it.contains("lowmemorykiller", ignoreCase = true) ||
                        it.contains("Out of memory", ignoreCase = true)
            }

            for (line in oomLines) {
                results.add(
                    SystemEvent(
                        timestamp = parseLogcatTimestamp(line),
                        category = EventCategory.OUT_OF_MEMORY,
                        severity = EventSeverity.ERROR,
                        processName = extractProcessName(line),
                        title = "Posible evento de memoria baja (OOM)",
                        rawDetails = line,
                        source = "logcat"
                    )
                )
            }
        } catch (e: Exception) {
            // Si falla logcat, no rompemos toda la recolección; DropBox sigue siendo la fuente principal
        }
        return results
    }

    /**
     * logcat -v time da formato "MM-dd HH:mm:ss.SSS" sin año (ej: "07-12 15:23:54.896").
     * Le agregamos el año actual, ya que logcat no lo incluye.
     */
    private fun parseLogcatTimestamp(line: String): Long {
        return try {
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val datePart = line.substring(0, 18) // "MM-dd HH:mm:ss.SSS" son 18 caracteres
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())
            format.parse("$currentYear-$datePart")?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis() // si el parseo falla, mejor una fecha aproximada que romper la recolección
        }
    }

    /**
     * Clasificador simple basado en la tag de DropBox y palabras clave del contenido.
     * Esto es un punto de partida; se puede refinar mucho más adelante.
     */
    private fun classifyEntry(tag: String, text: String, timestamp: Long): SystemEvent {
        val (category, severity, title) = when (tag) {
            "SYSTEM_LAST_KMSG" -> classifyKernelLog(text)
            "SYSTEM_TOMBSTONE" -> classifyTombstone(text)
            "system_app_crash" -> Triple(
                EventCategory.APP_CRASH,
                EventSeverity.ERROR,
                "Una app se cerró inesperadamente"
            )
            "system_app_anr" -> Triple(
                EventCategory.APP_ANR,
                EventSeverity.WARNING,
                "Una app dejó de responder (ANR)"
            )
            "system_app_wtf" -> Triple(
                EventCategory.UNKNOWN,
                EventSeverity.WARNING,
                "Error interno reportado por el sistema"
            )
            else -> Triple(EventCategory.UNKNOWN, EventSeverity.INFO, "Evento del sistema")
        }

        return SystemEvent(
            timestamp = timestamp,
            category = category,
            severity = severity,
            processName = extractProcessName(text),
            title = title,
            rawDetails = text,
            source = "dropbox"
        )
    }

    /**
     * SYSTEM_LAST_KMSG se genera en CADA arranque, sin importar el motivo.
     * Hay que leer la línea "Last boot reason:" para saber si fue un
     * reinicio normal/manual o una falla real del sistema.
     */
    private fun classifyKernelLog(text: String): Triple<EventCategory, EventSeverity, String> {
        val reasonRegex = Regex("""Last boot reason:\s*(\S+)""")
        val reason = reasonRegex.find(text)?.groupValues?.getOrNull(1)?.lowercase() ?: ""

        return when {
            reason.contains("power_key_longpress") || reason.contains("power_key") ->
                Triple(EventCategory.UNKNOWN, EventSeverity.INFO, "Reinicio manual (botón de encendido)")

            reason.contains("panic") ->
                Triple(EventCategory.KERNEL_PANIC, EventSeverity.ERROR, "Kernel panic real")

            reason.contains("wdt") || reason.contains("watchdog") ->
                Triple(EventCategory.WATCHDOG_RESET, EventSeverity.ERROR, "Reinicio forzado por watchdog")

            reason.contains("thermal") ->
                Triple(EventCategory.UNKNOWN, EventSeverity.WARNING, "Reinicio por sobrecalentamiento")

            reason.isEmpty() ->
                Triple(EventCategory.UNKNOWN, EventSeverity.INFO, "Reinicio (motivo no identificado)")

            else ->
                Triple(EventCategory.UNKNOWN, EventSeverity.INFO, "Reinicio: $reason")
        }
    }

    /**
     * SYSTEM_TOMBSTONE agrupa crashes nativos de todo tipo. El caso "TimeCheck timeout"
     * es particular: no es un bug de memoria ni un crash real de la app, sino el watchdog
     * interno de audioserver matando un hilo que tardó demasiado (común en HALs de audio
     * MediaTek al cambiar de ruta de audio, activar Bluetooth, etc.).
     */
    private fun classifyTombstone(text: String): Triple<EventCategory, EventSeverity, String> {
        return if (text.contains("TimeCheck timeout", ignoreCase = true)) {
            Triple(
                EventCategory.AUDIO_TIMEOUT,
                EventSeverity.WARNING,
                "Timeout del subsistema de audio (TimeCheck)"
            )
        } else {
            Triple(EventCategory.NATIVE_CRASH, EventSeverity.ERROR, "Crash nativo detectado")
        }
    }


    private fun extractProcessName(text: String): String? {
        // Busca patrones comunes tipo "Process: com.example.app" o "pid: 1234, ProcessName: com.example.app"
        val processRegex = Regex("""Process(?:Name)?[:\s]+([\w.]+)""")
        return processRegex.find(text)?.groupValues?.getOrNull(1)
    }
}
