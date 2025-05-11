package com.example.notify.ui.viewmodel // It's fine to keep it in this package

import android.content.Context // Import Context
import android.util.Log // Import Log for debugging
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notify.NotificationScheduler // Import your NotificationScheduler
import com.example.notify.data.Task // Import your Task data class
import com.example.notify.data.TaskDao // Import your TaskDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Modify the constructor to accept Context
class TaskViewModel(private val taskDao: TaskDao, private val context: Context) : ViewModel() {

    // Expose the list of tasks as a StateFlow from the database
    val allTasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Configure when the flow is active
            initialValue = emptyList()
        )

    fun insertTask(task: Task) {
        viewModelScope.launch {
            try {
                // Insert the task into the database and get the generated ID
                val newRowId = taskDao.insertTask(task)
                // Create a new Task object with the generated ID
                val newTaskWithId = task.copy(id = newRowId)
                // Room insert returns Long

                // Schedule the notification using the task with its generated ID
                NotificationScheduler.scheduleNotification(context, newTaskWithId)

                Log.d("TaskViewModel", "Task inserted with ID: ${newTaskWithId.id} and notification scheduled.")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error inserting task: ${e.message}", e)
                // Handle insertion errors if needed
            }
        }
    }

    /**
     * Deletes a task from the database and cancels its associated notification.
     * @param task The Task object to delete.
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                // 1. Delete the task from the Room database
                taskDao.deleteTask(task)
                Log.d("TaskViewModel", "Task ID ${task.id} deleted from database.")

                // 2. Cancel the scheduled notification for the deleted task
                NotificationScheduler.cancelNotification(context, task.id) // Pass the task ID for cancellation
                Log.d("TaskViewModel", "Notification for task ID ${task.id} cancelled.")

            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error deleting task ID ${task.id}: ${e.message}", e)
                // You might want to provide feedback to the user if deletion fails
            }
        }
    }

    // Add other functions for task operations (e.g., updateTask) if needed
}