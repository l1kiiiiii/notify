package com.example.notify.data

import android.content.Context

// Assuming your existing TaskDao and AppDatabase are correctly defined
// and AppDatabase has a method to get the TaskDao.

class TaskRepository(private val taskDao: TaskDao) { // Constructor injection for TaskDao is good practice

    suspend fun getUpcomingTasks(now: Long, limit: Int): List<String> {
        // This will call the suspend function defined in your TaskDao
        return taskDao.getUpcomingTasks(now, limit) as List<String>
    }

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getDatabase(context.applicationContext) // Use your existing AppDatabase
                val instance = TaskRepository(database.taskDao()) // Pass the DAO from your AppDatabase
                INSTANCE = instance
                instance
            }
        }
    }
}
