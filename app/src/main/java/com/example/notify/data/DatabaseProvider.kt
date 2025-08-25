// DatabaseProvider.kt
package com.example.notify.data

import android.content.Context

object DatabaseProvider {
    fun getDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context.applicationContext) // Call the one in AppDatabase
    }

    fun getTaskDao(context: Context): TaskDao {
        return getDatabase(context).taskDao()
    }
}