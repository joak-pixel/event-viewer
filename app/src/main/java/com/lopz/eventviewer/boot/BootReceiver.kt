package com.lopz.eventviewer.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lopz.eventviewer.collector.LogCollectorWorker

/**
 * Se dispara automáticamente cuando el sistema termina de bootear.
 * Su único trabajo es delegar la recolección pesada a un Worker,
 * porque un BroadcastReceiver tiene una ventana de tiempo muy corta
 * (unos segundos) antes de que el sistema lo mate.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val request = OneTimeWorkRequestBuilder<LogCollectorWorker>()
                .addTag("boot_collection")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
