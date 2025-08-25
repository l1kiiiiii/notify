package com.example.notify.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val details: String,
    val scheduledTimeMillis: Long,
    val category: String = "General",
    val priority: Priority = Priority.LOW,
    val status: TaskStatus = TaskStatus.PENDING
)
