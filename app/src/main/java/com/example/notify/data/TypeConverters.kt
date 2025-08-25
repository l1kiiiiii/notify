package com.example.notify.data

import androidx.room.TypeConverter

class AppTypeConverters {
    @TypeConverter
    fun fromPriority(priority: Priority?): String? {
        return priority?.name
    }

    @TypeConverter
    fun toPriority(priority: String?): Priority? {
        return priority?.let { Priority.valueOf(it) }
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toTaskStatus(status: String?): TaskStatus? {
        return status?.let { TaskStatus.valueOf(it) }
    }
}