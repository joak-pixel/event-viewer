package com.lopz.eventviewer

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lopz.eventviewer.data.EventSeverity
import com.lopz.eventviewer.data.SystemEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(private var events: List<SystemEvent> = emptyList()) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun submitList(newEvents: List<SystemEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position], dateFormat)
    }

    override fun getItemCount(): Int = events.size

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.eventTitle)
        private val subtitle: TextView = itemView.findViewById(R.id.eventSubtitle)
        private val indicator: View = itemView.findViewById(R.id.severityIndicator)

        fun bind(event: SystemEvent, dateFormat: SimpleDateFormat) {
            title.text = event.title
            val process = event.processName ?: "proceso desconocido"
            subtitle.text = "$process · ${dateFormat.format(Date(event.timestamp))}"

            indicator.setBackgroundColor(
                when (event.severity) {
                    EventSeverity.ERROR -> Color.parseColor("#F44336")
                    EventSeverity.WARNING -> Color.parseColor("#FFC107")
                    EventSeverity.INFO -> Color.parseColor("#2196F3")
                }
            )

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, EventDetailActivity::class.java)
                intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.id)
                itemView.context.startActivity(intent)
            }
        }
    }
}
