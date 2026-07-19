package com.lopz.eventviewer

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.switchMap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lopz.eventviewer.collector.LogCollectorWorker
import com.lopz.eventviewer.data.AppDatabase
import com.lopz.eventviewer.data.EventCategory
import com.lopz.eventviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = EventAdapter()

    // null = "Todos" (sin filtro). El índice del spinner coincide con el orden de esta lista.
    private val filterOptions: List<Pair<String, EventCategory?>> = listOf(
        "Todos" to null,
        "Kernel panic" to EventCategory.KERNEL_PANIC,
        "Memoria baja (OOM)" to EventCategory.OUT_OF_MEMORY,
        "Crash de apps" to EventCategory.APP_CRASH,
        "ANR (app no responde)" to EventCategory.APP_ANR,
        "Crash nativo" to EventCategory.NATIVE_CRASH,
        "Timeout de audio" to EventCategory.AUDIO_TIMEOUT,
        "Watchdog reset" to EventCategory.WATCHDOG_RESET,
        "Otros / desconocido" to EventCategory.UNKNOWN
    )

    private val selectedFilter = MutableLiveData<EventCategory?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventsRecyclerView.adapter = adapter

        setupCategoryFilterSpinner()

        val dao = AppDatabase.getInstance(applicationContext).systemEventDao()

        // Cada vez que cambia el filtro seleccionado, cambiamos qué query de la DB observamos
        val filteredEvents = selectedFilter.switchMap { category ->
            if (category == null) dao.getAllEvents() else dao.getEventsByCategory(category)
        }

        filteredEvents.observe(this, Observer { events ->
            adapter.submitList(events)
            binding.emptyText.visibility =
                if (events.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        })

        // Botón de test: dispara la misma recolección que corre automáticamente al bootear,
        // útil para validar que READ_LOGS esté bien otorgado sin tener que reiniciar el equipo
        binding.btnTestCollect.setOnClickListener {
            val request = OneTimeWorkRequestBuilder<LogCollectorWorker>().build()
            WorkManager.getInstance(applicationContext).enqueue(request)
            Toast.makeText(this, "Recolectando eventos...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategoryFilterSpinner() {
        val labels = filterOptions.map { it.first }
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categoryFilterSpinner.adapter = spinnerAdapter

        binding.categoryFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedFilter.value = filterOptions[position].second
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedFilter.value = null
            }
        }
    }
}