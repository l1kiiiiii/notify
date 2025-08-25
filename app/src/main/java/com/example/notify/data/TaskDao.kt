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

    @Query("SELECT * FROM tasks WHERE category = :categoryName ORDER BY scheduledTimeMillis ASC")
    fun getTasksByCategory(categoryName: String): Flow<List<Task>>

    @Query("SELECT DISTINCT category FROM tasks ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM tasks WHERE category = :categoryName AND scheduledTimeMillis > :currentTimeMillis ORDER BY scheduledTimeMillis ASC")
    fun getUpcomingTasksByCategory(categoryName: String, currentTimeMillis: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY priority ASC, scheduledTimeMillis ASC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

    // Fetches tasks that are not completed and are either explicitly upcoming, pending, or in-progress
    // This allows the ViewModel to further refine what "upcoming" means (e.g., based on time).
    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' AND scheduledTimeMillis > :currentTimeMillis ORDER BY scheduledTimeMillis ASC") // Added AND scheduledTimeMillis > :currentTimeMillis
    fun getPotentiallyUpcomingTasks(currentTimeMillis: Long): Flow<List<Task>>



    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY scheduledTimeMillis DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    // You might also want a query to get all non-completed tasks sorted by priority then time
    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY priority ASC, scheduledTimeMillis ASC")
    fun getAllActiveTasksSorted(): Flow<List<Task>>
}