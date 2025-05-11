package com.example.notify.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.NotificationScheduler
import com.example.notify.data.DatabaseProvider
import com.example.notify.data.Task
import com.example.notify.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Create(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(LocalContext.current)),
    notificationScheduler: NotificationScheduler = NotificationScheduler
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDetails by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val coroutineScope = rememberCoroutineScope()

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    val allTasks by taskViewModel.allTasks.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Schedule Task") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = taskDetails,
                onValueChange = { taskDetails = it },
                label = { Text("Task Details") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { datePickerDialog.show() }) {
                    Text(selectedDate?.let {
                        "${it.get(Calendar.DAY_OF_MONTH)}/${it.get(Calendar.MONTH) + 1}/${it.get(Calendar.YEAR)}"
                    } ?: "Select Date")
                }

                Button(onClick = { timePickerDialog.show() }) {
                    Text(selectedTime?.let {
                        "${it.get(Calendar.HOUR_OF_DAY)}:${it.get(Calendar.MINUTE).toString().padStart(2, '0')}"
                    } ?: "Select Time")
                }
            }


            // This is the "SET" button that should schedule the task
            Button(
                onClick = {
                    Log.d("CreateScreen", "SET button clicked")

                    if (taskTitle.isNotBlank() && selectedDate != null && selectedTime != null) {
                        val taskCalendar = Calendar.getInstance().apply {
                            set(
                                selectedDate!!.get(Calendar.YEAR),
                                selectedDate!!.get(Calendar.MONTH),
                                selectedDate!!.get(Calendar.DAY_OF_MONTH)
                            )
                            set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                        }

                        val task = Task(
                            title = taskTitle,
                            details = taskDetails,
                            scheduledTimeMillis = taskCalendar.timeInMillis
                        )

                        taskViewModel.insertTask(task)

                        coroutineScope.launch {
                            notificationScheduler.scheduleNotification(
                                context,
                                task.title,
                                task.details,
                                task.scheduledTimeMillis
                            )
                        }

                        // Add feedback for the user
                        Toast.makeText(context, "Task scheduled!", Toast.LENGTH_SHORT).show()

                        // Clear fields after successful scheduling
                        taskTitle = ""
                        taskDetails = ""
                        selectedDate = null
                        selectedTime = null
                    } else {
                        Toast.makeText(context, "Please fill all fields and select date/time", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SET") // Text displayed on the button
            }
        }
    }
}

class TaskViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(DatabaseProvider.getTaskDao(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}