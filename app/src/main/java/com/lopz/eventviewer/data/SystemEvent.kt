package com.lopz.eventviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventSeverity { ERROR, WARNING, INFO }

enum class EventCategory {
    KERNEL_PANIC,       // SYSTEM_LAST_KMSG - el equipo se reinició por un panic del kernel
    OUT_OF_MEMORY,      // lowmemorykiller / OOM killer actuando
    APP_CRASH,          // system_app_crash - una app se cerró de golpe
    APP_ANR,            // system_app_anr - una app dejó de responder
    NATIVE_CRASH,       // SYSTEM_TOMBSTONE - crash a nivel nativo (C/C++)
    AUDIO_TIMEOUT,      // TimeCheck timeout del audioserver - HAL de audio tardó demasiado
    WATCHDOG_RESET,     // reinicio forzado por watchdog del sistema
    UNKNOWN
}

@Entity(tableName = "system_events")
data class SystemEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,          // epoch millis de cuándo ocurrió el evento
    val category: EventCategory,
    val severity: EventSeverity,
    val processName: String?,     // proceso/app involucrado, si se pudo determinar
    val title: String,            // resumen corto, ej: "OOM Killer terminó com.whatsapp"
    val rawDetails: String,       // el texto crudo del log, para el detalle expandible
    val source: String            // "dropbox" o "logcat", de dónde vino el dato
)
