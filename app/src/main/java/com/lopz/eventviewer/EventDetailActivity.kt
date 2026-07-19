package com.lopz.eventviewer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lopz.eventviewer.data.AppDatabase
import com.lopz.eventviewer.data.SystemEvent
import com.lopz.eventviewer.databinding.ActivityEventDetailBinding
import com.lopz.eventviewer.util.LogSummarizer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }

    private lateinit var binding: ActivityEventDetailBinding
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId == -1L) {
            finish()
            return
        }

        val dao = AppDatabase.getInstance(applicationContext).systemEventDao()
        lifecycleScope.launch {
            val event = dao.getEventById(eventId)
            if (event == null) {
                finish()
                return@launch
            }

            binding.detailTitle.text = event.title
            binding.detailSubtitle.text =
                "${event.processName ?: "proceso desconocido"} · ${dateFormat.format(Date(event.timestamp))}"
            binding.detailRaw.text = LogSummarizer.summarize(event.rawDetails)

            binding.btnShare.setOnClickListener {
                shareEvent(event)
            }
        }
    }

    /**
     * Comparte un resumen legible del evento (no el log completo, que puede tener
     * miles de líneas y no sirve para pegar en un chat). El log resumido queda
     * disponible igual por si alguien quiere más detalle técnico.
     */
    private fun shareEvent(event: SystemEvent) {
        val shareText = buildString {
            appendLine("Event Viewer - Reporte de evento")
            appendLine()
            appendLine("Título: ${event.title}")
            appendLine("Categoría: ${event.category}")
            appendLine("Severidad: ${event.severity}")
            appendLine("Proceso: ${event.processName ?: "desconocido"}")
            appendLine("Fecha: ${dateFormat.format(Date(event.timestamp))}")
            appendLine("Fuente: ${event.source}")
            appendLine()
            appendLine("--- Detalle técnico (resumido) ---")
            appendLine(LogSummarizer.summarize(event.rawDetails))
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Event Viewer: ${event.title}")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(intent, "Compartir evento vía"))
    }
}