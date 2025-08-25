package com.example.notify.data

enum class TaskStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In-Progress"),
    UPCOMING("Upcoming"), // This might be determined by scheduledTime or set manually
    COMPLETED("Completed")
}