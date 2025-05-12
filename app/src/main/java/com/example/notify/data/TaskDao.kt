package com.example.notify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task): Long

    @Query("SELECT * FROM tasks ORDER BY scheduledTimeMillis ASC")
    fun getAllTasks(): Flow<List<Task>>

    // Add the delete method
    @Delete
    suspend fun deleteTask(task: Task) // Room will use the primary key (id) to delete

    // You might also want a method to get a single task by ID if needed
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE scheduledTimeMillis >= :currentTime ORDER BY scheduledTimeMillis ASC")
    fun getUpcomingTasks(currentTime: Long): Flow<List<Task>>
}