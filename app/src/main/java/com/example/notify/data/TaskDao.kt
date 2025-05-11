package com.example.notify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: Task): Long

    @Query("SELECT * FROM tasks ORDER BY scheduledTimeMillis ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)
}