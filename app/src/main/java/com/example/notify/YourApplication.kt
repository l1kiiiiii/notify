package com.example.notify

import android.app.Application
import androidx.room.Room
import com.example.notify.data.AppDatabase

class YourApplication : Application() {
    lateinit var taskDatabase: AppDatabase

    override fun onCreate() {
        super.onCreate()
        taskDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "task_database"
        ).build()
    }
}