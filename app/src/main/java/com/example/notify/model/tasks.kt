package com.example.notify.model

data class Task(
    val id: Long,
    val title: String="",
    val details: String="",
    val scheduledTimeMillis: Long)