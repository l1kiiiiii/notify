package com.example.notify.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notify.data.DatabaseProvider // Import DatabaseProvider

class TaskViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            // Get the TaskDao from your DatabaseProvider
            val taskDao = DatabaseProvider.getDatabase(context).taskDao()
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao, context) as T // Pass taskDao and context
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}