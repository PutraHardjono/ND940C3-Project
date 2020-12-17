package com.udacity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

private const val NOTIFICATION_ID = 0
private const val REQUEST_CODE = 0

fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context, downloadInfo: DownloadInfo) {

    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Add snooze button to Notification
    val detailIntent = Intent(applicationContext, DetailActivity::class.java)
    detailIntent.putExtra(
            applicationContext.getString(R.string.download_info_intent_name),
            downloadInfo
    )
    val statusPendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Build Notification
    val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_channel_id)
    )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(
                    R.drawable.ic_assistant_black_24dp,
                    applicationContext.getString(R.string.snooze_button_description),
                    statusPendingIntent
            )

    notify(NOTIFICATION_ID, builder.build())
}