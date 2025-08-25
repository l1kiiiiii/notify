package com.example.notify.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notify.NotificationScheduler
import com.example.notify.data.Priority
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

    // States for search queries
    private val _searchQuery = MutableStateFlow("") // Renamed from _allTasksSearchQuery
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow() // Renamed from allTasksSearchQuery

    private val _upcomingSearchQuery = MutableStateFlow("")
    val upcomingSearchQuery: StateFlow<String> = _upcomingSearchQuery.asStateFlow()

    private val _sortUpcomingByPriority = MutableStateFlow(false)
    val sortUpcomingByPriority: StateFlow<Boolean> = _sortUpcomingByPriority.asStateFlow()

    // Flow of all tasks from the database
    private val _allTasks: StateFlow<List<Task>> = taskDao.getAllTasks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Filtered flow for the AllTasks screen, combining all tasks and search query
    val filteredTasks: StateFlow<List<Task>> = combine(_allTasks, searchQuery) { tasks, query ->
        filterTasks(tasks, query)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Flow of upcoming tasks from the database
    private val _upcomingTasks: StateFlow<List<Task>> = taskDao.getUpcomingTasks(Calendar.getInstance().timeInMillis).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Filtered and sorted flow for the HomeScreen
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredUpcomingTasks: StateFlow<List<Task>> = combine(
        _upcomingTasks,
        upcomingSearchQuery,
        sortUpcomingByPriority
    ) { tasks, query, sortByPriority ->
        val filtered = filterTasks(tasks, query)
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
            val newRowId = taskDao.insertTask(task)
            if (newRowId > 0) {
                val taskWithId = task.copy(id = newRowId)
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
                taskDao.updateTask(it.copy(status = newStatus))
            }
        }
    }

    fun updateSearchQuery(query: String) { // Renamed from updateAllTasksSearchQuery
        _searchQuery.value = query
    }

    fun updateUpcomingSearchQuery(query: String) {
        _upcomingSearchQuery.value = query
    }

    private fun filterTasks(tasks: List<Task>, query: String): List<Task> {
        return if (query.isBlank()) {
            tasks
        } else {
            tasks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.details.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
    }
}