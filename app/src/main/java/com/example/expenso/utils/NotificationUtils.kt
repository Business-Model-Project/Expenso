package com.example.expenso.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.expenso.R

object NotificationUtils {
    private const val CHANNEL_ID = "budget_channel"
    private const val NOTIFICATION_ID = 101
    private var channelCreated = false

    fun sendBudgetExceededNotification(context: Context, total: Double, budget: Double) {
        if (!channelCreated) {
            createNotificationChannel(context)
            channelCreated = true
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Budget Exceeded!")
            .setContentText("You spent €${"%.2f".format(total)} (Budget: €${"%.2f".format(budget)})")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true) // Prevent repeated alerts

        try {
            with(NotificationManagerCompat.from(context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (areNotificationsEnabled()) {
                        notify(NOTIFICATION_ID, builder.build())
                    }
                } else {
                    notify(NOTIFICATION_ID, builder.build())
                }
            }
        } catch (e: Exception) {
            // Handle any notification errors silently
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Budget Alerts"
            val descriptionText = "Notifications when you exceed monthly budget"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private const val PASSWORD_CHANGE_CHANNEL_ID = "password_change_channel"
    private const val PASSWORD_CHANGE_NOTIFICATION_ID = 1002

    fun sendPasswordChangedNotification(context: Context) {
        createPasswordChangeChannel(context)

        val notification = NotificationCompat.Builder(context, PASSWORD_CHANGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_landing) // Use appropriate icon
            .setContentTitle("Password Changed")
            .setContentText("Your password has been changed successfully")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(PASSWORD_CHANGE_NOTIFICATION_ID, notification)
    }

    private fun createPasswordChangeChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PASSWORD_CHANGE_CHANNEL_ID,
                "Password Changes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for password changes"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}