package com.example.notify // Replace with your package name

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.notify.data.Task

object NotificationScheduler {

    internal const val CHANNEL_ID = "task_notification_channel"
    private const val CHANNEL_NAME = "Task Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for scheduled tasks"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(context: Context, task: Task, requestCode: Long) { // Added requestCode: Long
        Log.d("NotificationScheduler", "Scheduling notification for task ID: ${task.id} with requestCode: $requestCode at ${java.util.Date(task.scheduledTimeMillis)}")

        createNotificationChannel(context)
        Log.d("NotificationScheduler", "Notification channel created/checked.")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Log.d("NotificationScheduler", "AlarmManager obtained.")

        val intent: Intent = Intent(context, NotificationReceiver::class.java).apply {
            // Pass necessary task data as extras
            putExtra("task_id", task.id)
            putExtra("title", task.title)
            putExtra("message", task.details) // Assuming 'details' is the message
        }
        Log.d("NotificationScheduler", "Intent for NotificationReceiver created.")

        // Use the provided requestCode for the PendingIntent
        Log.d("NotificationScheduler", "Using PendingIntent request code: $requestCode")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode.toInt(), // Convert Long requestCode to Int for PendingIntent (if needed, check PendingIntent docs)
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("NotificationScheduler", "PendingIntent created.")

        // Schedule the alarm
        val currentTime = System.currentTimeMillis()
        if (task.scheduledTimeMillis > currentTime) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.scheduledTimeMillis,
                    pendingIntent
                )
                Log.d("NotificationScheduler", "setExactAndAllowWhileIdle called for task ID: ${task.id} with requestCode: $requestCode")
            } catch (e: SecurityException) {
                Log.e("NotificationScheduler", "SecurityException scheduling alarm for task ID ${task.id} with requestCode ${requestCode}: ${e.message}", e)
            } catch (e: Exception) {
                Log.e("NotificationScheduler", "Error scheduling alarm for task ID ${task.id} with requestCode ${requestCode}: ${e.message}", e)
            }
            Log.d("NotificationScheduler", "Alarm scheduling attempt finished for task ID: ${task.id} with requestCode: $requestCode")
        } else {
            Log.d("NotificationScheduler", "Task ID ${task.id} scheduled in the past, not scheduling alarm.")
        }
    }

    fun cancelNotification(context: Context, requestCode: Long) { // Use Long here too
        Log.d("NotificationScheduler", "Attempting to cancel notification for requestCode: $requestCode")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Recreate the PendingIntent using the same request code and intent structure
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            // It's good practice to include the task ID in the intent even for cancellation,
            // although the requestCode is the primary identifier for cancel().
            putExtra("task_id", requestCode) // Use the requestCode (task ID)
            // Include other extras if your PendingIntent recreation strictly requires them,
            // but for cancel() the requestCode is usually sufficient with the correct component/action.
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode.toInt(), // Convert Long requestCode to Int for PendingIntent (if needed)
            intent,
            // Use the same flags as when scheduling!
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
        Log.d("NotificationScheduler", "Cancelled notification with request code: $requestCode")
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}