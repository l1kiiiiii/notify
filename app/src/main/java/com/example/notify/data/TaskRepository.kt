package com.example.notify.data

import android.content.Context

// Repository that provides a clean API to access TaskDao
class TaskRepository private constructor(
    private val taskDao: TaskDao
) {

    // Returns formatted strings for upcoming tasks (for widgets, one-shot)
    suspend fun getUpcomingTasksForWidget(limit: Int): List<String> {
        val currentTime = System.currentTimeMillis()
        return taskDao.getUpcomingTaskStrings(currentTime, limit)
    }

    // Example: You could also expose Flow<List<Task>> for Compose screens
    // fun getUpcomingTasksFlow(limit: Int): Flow<List<Task>> =
    //     taskDao.getUpcomingTasks(System.currentTimeMillis(), limit)

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val instance = TaskRepository(database.taskDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
