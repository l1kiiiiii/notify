package com.example.notify.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var database: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database" // Your database name
            ).build()
            database = instance
            instance
        }
    }
    fun getTaskDao(context: Context): TaskDao {
        return getDatabase(context).taskDao()
    }
}