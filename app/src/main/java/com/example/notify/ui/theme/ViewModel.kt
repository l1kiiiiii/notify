package com.example.notify.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notify.NotificationScheduler
import com.example.notify.data.Task
import com.example.notify.data.TaskDao
import com.example.notify.data.TaskStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(private val taskDao: TaskDao, private val context: Context) : ViewModel() {

    // Removed: _searchQuery and searchQuery for general tasks

    private val _upcomingSearchQuery = MutableStateFlow("")
    val upcomingSearchQuery: StateFlow<String> = _upcomingSearchQuery.asStateFlow()

    private val _sortUpcomingByPriority = MutableStateFlow(false)
    val sortUpcomingByPriority: StateFlow<Boolean> = _sortUpcomingByPriority.asStateFlow()

    private val UPCOMING_TASKS_DISPLAY_LIMIT = 5

    private val _allTasks: StateFlow<List<Task>> = taskDao.getAllTasks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Updated: filteredTasks now directly exposes all tasks without general search filtering
    val filteredTasks: StateFlow<List<Task>> = _allTasks

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredUpcomingTasks: StateFlow<List<Task>> = combine(
        taskDao.getUpcomingTasks(Calendar.getInstance().timeInMillis, UPCOMING_TASKS_DISPLAY_LIMIT),
        upcomingSearchQuery,
        sortUpcomingByPriority
    ) { tasks, query, sortByPriority ->
        val filtered = filterTasks(tasks, query) // filterTasks is still used here
        if (sortByPriority) {
            filtered.sortedWith(compareBy<Task> { it.priority }.thenBy { it.scheduledTimeMillis })
        } else {
            filtered.sortedBy { it.scheduledTimeMillis }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun insertTask(task: Task) {
        viewModelScope.launch {
            val initialStatus = determineTaskStatus(task.scheduledTimeMillis)
            val newTask = task.copy(status = initialStatus)
            val newRowId = taskDao.insertTask(newTask)
            if (newRowId > 0) {
                val taskWithId = newTask.copy(id = newRowId)
                NotificationScheduler.scheduleNotification(context, taskWithId, newRowId)
            }
        }
    }

    fun getTaskById(taskId: Long): Flow<Task?> {
        return taskDao.getTaskById(taskId)
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            NotificationScheduler.cancelNotification(context, task.id)
            taskDao.deleteTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            NotificationScheduler.cancelNotification(context, task.id)
            taskDao.updateTask(task)
            NotificationScheduler.scheduleNotification(context, task, task.id)
        }
    }

    fun updateTaskStatus(taskId: Long, newStatus: TaskStatus) {
        viewModelScope.launch {
            val task = taskDao.getTaskById(taskId).firstOrNull()
            task?.let {
                val updatedTask = when (newStatus) {
                    TaskStatus.COMPLETED -> it.copy(status = newStatus)
                    else -> it.copy(status = determineTaskStatus(it.scheduledTimeMillis))
                }
                taskDao.updateTask(updatedTask)
                NotificationScheduler.cancelNotification(context, task.id)
                NotificationScheduler.scheduleNotification(context, updatedTask, task.id)
            }
        }
    }

    private fun determineTaskStatus(scheduledTimeMillis: Long?): TaskStatus {
        val currentTime = System.currentTimeMillis()
        return if (scheduledTimeMillis != null && scheduledTimeMillis > currentTime) {
            TaskStatus.UPCOMING
        } else {
            TaskStatus.ACTIVE
        }
    }

    // Removed: updateSearchQuery function for general tasks

    fun updateUpcomingSearchQuery(query: String) {
        _upcomingSearchQuery.value = query
    }

    // filterTasks is kept as it's used by filteredUpcomingTasks
    private fun filterTasks(tasks: List<Task>, query: String): List<Task> {
        return if (query.isBlank()) {
            tasks
        } else {
            tasks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.details?.contains(query, ignoreCase = true) ?: false) ||
                        (it.category?.contains(query, ignoreCase = true) ?: false)
            }
        }
    }
}