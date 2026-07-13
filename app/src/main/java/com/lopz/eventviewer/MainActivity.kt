package com.lopz.eventviewer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lopz.eventviewer.collector.LogCollectorWorker
import com.lopz.eventviewer.data.AppDatabase
import com.lopz.eventviewer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = EventAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.eventsRecyclerView.adapter = adapter

        // Observamos la DB en vivo: cualquier evento nuevo insertado se refleja automático
        val dao = AppDatabase.getInstance(applicationContext).systemEventDao()
        dao.getAllEvents().observe(this, Observer { events ->
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
}
