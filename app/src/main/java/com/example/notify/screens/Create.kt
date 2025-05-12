package com.example.notify.screens

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.data.DatabaseProvider
import com.example.notify.data.Task
import com.example.notify.ui.viewmodel.TaskViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Create(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(LocalContext.current)),
    // Note: NotificationScheduler is an object, you don't need to provide it as a parameter like this
    // You can directly call NotificationScheduler methods from the ViewModel or here if needed
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDetails by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // --- START: Add the permission handling code here ---

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "POST_NOTIFICATIONS permission granted")
            // Optionally trigger scheduling logic if this was the last permission needed
            // Or just rely on the button click calling scheduleTaskWithPermissionCheck again
        } else {
            Log.d("Permissions", "POST_NOTIFICATIONS permission denied")
            Toast.makeText(context, "Notification permission denied. Notifications are needed for reminders.", Toast.LENGTH_LONG).show()
        }
    }

    // Function containing the core task scheduling logic
    fun scheduleTaskLogic() {
        Log.d("CreateScreen", "Executing scheduleTaskLogic")
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

            // Insert the task into the database and schedule the notification via the ViewModel
            taskViewModel.insertTask(task)

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
    }

    // Function to check permissions and then call scheduleTaskLogic
    fun scheduleTaskWithPermissionCheck() {
        Log.d("CreateScreen", "Checking permissions before scheduling")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // For POST_NOTIFICATIONS (API 33+)
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("CreateScreen", "POST_NOTIFICATIONS already granted. Checking exact alarm.")
                    // Permission already granted, proceed to check for exact alarm permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // For SCHEDULE_EXACT_ALARM (API 31+)
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (alarmManager.canScheduleExactAlarms()) {
                            Log.d("CreateScreen", "canScheduleExactAlarms granted. Scheduling task.")
                            scheduleTaskLogic()
                        } else {
                            Log.d("CreateScreen", "Exact alarm permission needed. Redirecting to settings.")
                            Toast.makeText(context, "Please allow exact alarms for timely reminders in app settings.", Toast.LENGTH_LONG).show()
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            context.startActivity(intent)
                        }
                    } else {
                        // For older versions, exact alarms might not require special permission check here
                        Log.d("CreateScreen", "Pre-API 31. Scheduling task directly.")
                        scheduleTaskLogic()
                    }
                }
                // Note: In a real app, you might want to handle shouldShowRequestPermissionRationale
                // using a dialog or another UI element to explain *before* requesting.
                // For simplicity here, we'll just request directly or show a toast after denial.
                // shouldShowRequestPermissionRationale(...) -> { ... }
                else -> {
                    Log.d("CreateScreen", "Requesting POST_NOTIFICATIONS permission.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For older Android versions, POST_NOTIFICATIONS is granted at install time.
            Log.d("CreateScreen", "Pre-API 33. Checking exact alarm.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // For SCHEDULE_EXACT_ALARM (API 31+)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d("CreateScreen", "canScheduleExactAlarms granted (Pre-API 33). Scheduling task.")
                    scheduleTaskLogic()
                } else {
                    Log.d("CreateScreen", "Exact alarm permission needed (Pre-API 33). Redirecting to settings.")
                    Toast.makeText(context, "Please allow exact alarms for timely reminders in app settings.", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                }
            } else {
                // For older versions, exact alarms might not require special permission check here
                Log.d("CreateScreen", "Pre-API 31 (and Pre-API 33). Scheduling task directly.")
                scheduleTaskLogic()
            }
        }
    }

    // --- END: Add the permission handling code here ---


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

    // You might not need to collect all tasks in the Create screen unless you plan to display them
    // val allTasks by taskViewModel.allTasks.collectAsState()


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
                    // Call the permission check function instead of the direct logic
                    scheduleTaskWithPermissionCheck()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SET") // Text displayed on the button
            }
        }
    }
}

// Keep your TaskViewModelFactory here
class TaskViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(DatabaseProvider.getTaskDao(context),context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}