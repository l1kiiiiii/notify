package com.example.notify

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

abstract class NotificationReceiver : BroadcastReceiver() {

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(context: Context, title: String, message: String, timeInMillis: Long) {
        Log.d("NotificationScheduler", "Scheduling notification for: $title at ${java.util.Date(timeInMillis)}")

        createNotificationChannel(context)
        Log.d("NotificationScheduler", "Notification channel created/checked.")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Log.d("NotificationScheduler", "AlarmManager obtained.")

        val intent: Intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            // Consider adding a unique ID for the task here as well
            // putExtra("task_id", taskId)
        }
        Log.d("NotificationScheduler", "Intent for NotificationReceiver created.")

        // Use a unique request code!
        val requestCode = timeInMillis.toInt() // Use task ID if available
        Log.d("NotificationScheduler", "Using PendingIntent request code: $requestCode")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode, // Use the unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("NotificationScheduler", "PendingIntent created.")

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                Log.d("NotificationScheduler", "setExactAndAllowWhileIdle called.")
            } catch (e: SecurityException) {
                Log.e("NotificationScheduler", "SecurityException scheduling alarm: ${e.message}", e)
                // This could indicate missing SCHEDULE_EXACT_ALARM permission or issue with battery optimization settings
            } catch (e: Exception) {
                Log.e("NotificationScheduler", "Error scheduling alarm: ${e.message}", e)
            }
        } else {
            try {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
                Log.d("NotificationScheduler", "setExact called.")
            } catch (e: Exception) {
                Log.e("NotificationScheduler", "Error scheduling alarm (pre-M): ${e.message}", e)
            }
        }
        Log.d("NotificationScheduler", "Alarm scheduling attempt finished.")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notifications"
            val descriptionText = "Notifications for scheduled tasks"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                NotificationScheduler.CHANNEL_ID, // Use the same CHANNEL_ID from NotificationScheduler
                name,
                importance
            ).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}