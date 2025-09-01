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
    suspend fun insertTask(task: Task): Long // returns row ID

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // --- Basic Queries ---

    @Query("SELECT * FROM tasks ORDER BY scheduledTimeMillis ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    fun getTaskById(taskId: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE category = :categoryName ORDER BY scheduledTimeMillis ASC")
    fun getTasksByCategory(categoryName: String): Flow<List<Task>>

    @Query("SELECT DISTINCT category FROM tasks ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM tasks WHERE category = :categoryName AND scheduledTimeMillis > :currentTimeMillis ORDER BY scheduledTimeMillis ASC")
    fun getUpcomingTasksByCategory(categoryName: String, currentTimeMillis: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY priority ASC, scheduledTimeMillis ASC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

    // --- Upcoming / Active Queries ---

    // All non-completed tasks that are scheduled in the future
    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' AND scheduledTimeMillis > :currentTimeMillis ORDER BY scheduledTimeMillis ASC")
    fun getPotentiallyUpcomingTasks(currentTimeMillis: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY scheduledTimeMillis DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status != 'COMPLETED' ORDER BY priority ASC, scheduledTimeMillis ASC")
    fun getAllActiveTasksSorted(): Flow<List<Task>>

    // --- Compact Queries for Widgets / Summaries ---

    @Query(
        "SELECT * FROM tasks " +
                "WHERE scheduledTimeMillis > :currentTimeMillis " +
                "ORDER BY scheduledTimeMillis ASC " +
                "LIMIT :limit"
    )
    fun getUpcomingTasks(currentTimeMillis: Long, limit: Int): Flow<List<Task>>

    @Query(
        "SELECT title || ' (Scheduled: ' || " +
                "DATETIME(scheduledTimeMillis/1000, 'unixepoch', 'localtime') || ')' " +
                "FROM tasks " +
                "WHERE scheduledTimeMillis > :currentTimeMillis AND status != 'COMPLETED' " +
                "ORDER BY scheduledTimeMillis ASC " +
                "LIMIT :limit"
    )
    suspend fun getUpcomingTaskStrings(currentTimeMillis: Long, limit: Int): List<String>
}
