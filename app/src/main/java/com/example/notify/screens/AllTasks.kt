package com.example.notify.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.notify.data.DatabaseProvider
import com.example.notify.data.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTasks(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val taskDao = remember { database.taskDao() }

    val allTasks by taskDao.getAllTasks().collectAsState(initial = emptyList()) // Observe tasks as State

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("All Scheduled Tasks") })
        }
    ) { innerPadding ->
        if (allTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks scheduled yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(allTasks) { task ->
                    TaskItem(task = task)
                    Divider() // Add a divider between tasks
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(task.scheduledTimeMillis))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = task.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = task.details, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Scheduled for: $formattedTime", style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
fun AllTasksPreview() {
    AllTasks()
}