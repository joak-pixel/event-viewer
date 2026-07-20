package com.lopz.eventviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.lopz.eventviewer.util.NotificationHelper

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

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    "Sin ese permiso, no vas a recibir avisos de eventos nuevos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createChannel(applicationContext)
        requestNotificationPermissionIfNeeded()

        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventsRecyclerView.adapter = adapter

        setupCategoryFilterSpinner()

        val dao = AppDatabase.getInstance(applicationContext).systemEventDao()

        val filteredEvents = selectedFilter.switchMap { category ->
            if (category == null) dao.getAllEvents() else dao.getEventsByCategory(category)
        }

        filteredEvents.observe(this, Observer { events ->
            adapter.submitList(events)
            binding.emptyText.visibility =
                if (events.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        })

        binding.btnTestCollect.setOnClickListener {
            val request = OneTimeWorkRequestBuilder<LogCollectorWorker>().build()
            WorkManager.getInstance(applicationContext).enqueue(request)
            Toast.makeText(this, "Recolectando eventos...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Desde Android 13 (API 33), mostrar notificaciones requiere permiso explícito
     * del usuario en tiempo de ejecución, no alcanza con declararlo en el manifest.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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