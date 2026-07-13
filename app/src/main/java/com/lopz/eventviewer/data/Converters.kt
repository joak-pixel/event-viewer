package com.lopz.eventviewer.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(value: EventCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): EventCategory =
        try { EventCategory.valueOf(value) } catch (e: Exception) { EventCategory.UNKNOWN }

    @TypeConverter
    fun fromSeverity(value: EventSeverity): String = value.name

    @TypeConverter
    fun toSeverity(value: String): EventSeverity =
        try { EventSeverity.valueOf(value) } catch (e: Exception) { EventSeverity.INFO }
}
