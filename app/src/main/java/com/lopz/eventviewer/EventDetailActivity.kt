package com.lopz.eventviewer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lopz.eventviewer.data.AppDatabase
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

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            binding.detailTitle.text = event.title
            binding.detailSubtitle.text =
                "${event.processName ?: "proceso desconocido"} · ${dateFormat.format(Date(event.timestamp))}"
            binding.detailRaw.text = LogSummarizer.summarize(event.rawDetails)
        }
    }
}
