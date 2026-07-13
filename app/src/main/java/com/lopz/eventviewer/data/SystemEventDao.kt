package com.lopz.eventviewer.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SystemEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: SystemEvent): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(events: List<SystemEvent>)

    // Timeline principal, más reciente primero
    @Query("SELECT * FROM system_events ORDER BY timestamp DESC")
    fun getAllEvents(): LiveData<List<SystemEvent>>

    @Query("SELECT * FROM system_events WHERE category = :category ORDER BY timestamp DESC")
    fun getEventsByCategory(category: EventCategory): LiveData<List<SystemEvent>>

    @Query("SELECT * FROM system_events WHERE id = :id")
    suspend fun getEventById(id: Long): SystemEvent?

    // Para evitar duplicados al re-procesar el mismo log dos veces
    @Query("SELECT COUNT(*) FROM system_events WHERE timestamp = :timestamp AND rawDetails = :rawDetails")
    suspend fun exists(timestamp: Long, rawDetails: String): Int
}
