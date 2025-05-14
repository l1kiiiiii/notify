package com.example.notify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task): Long // Added return type Long for inserted row ID

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // This function should return a Flow<List<Task>> and NOT be suspend
    @Query("SELECT * FROM tasks ORDER BY scheduledTimeMillis ASC")
    fun getAllTasks(): Flow<List<Task>>

    // This function should return a Flow<List<Task>> and NOT be suspend
    @Query("SELECT * FROM tasks WHERE scheduledTimeMillis > :currentTimeMillis ORDER BY scheduledTimeMillis ASC")
    fun getUpcomingTasks(currentTimeMillis: Long): Flow<List<Task>>

    // This function should return a Flow<Task?> and NOT be suspend
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    fun getTaskById(taskId: Long): Flow<Task?> // Corrected return type and removed suspend

}