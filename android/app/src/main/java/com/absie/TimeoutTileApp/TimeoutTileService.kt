package com.absie.timeouttileapp

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast

class TimeoutTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d("TimeoutTileService", "Tile added")
        requestListeningState(
                applicationContext,
                ComponentName(this, TimeoutTileService::class.java)
        )
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile.label = "Screen Timeout"
        qsTile.updateTile()
        Log.d("TimeoutTileService", "Listening started")
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.System.canWrite(this)) {
            Toast.makeText(this, "Grant write settings permission", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
            return
        }

        val currentTimeout =
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 15000)

        val newTimeout =
                when (currentTimeout) {
                    15000 -> 30000
                    30000 -> Int.MAX_VALUE
                    else -> 15000
                }

        val success =
                Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_OFF_TIMEOUT,
                        newTimeout
                )

        val message =
                when (newTimeout) {
                    15000 -> "Screen Timeout set to 15 seconds"
                    30000 -> "Screen Timeout set to 30 seconds"
                    else -> "Screen Timeout set to Never"
                }

        showNotification(if (success) message else "Failed to set timeout")
        Log.d("TimeoutTileService", message)
    }
    private fun showNotification(message: String) {
        val channelId = "timeout_tile_channel"
        val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                    android.app.NotificationChannel(
                            channelId,
                            "Timeout Tile Alerts",
                            android.app.NotificationManager.IMPORTANCE_DEFAULT
                    )
            notificationManager.createNotificationChannel(channel)
        }

        val notification =
                android.app.Notification.Builder(this, channelId)
                        .setContentTitle("Timeout")
                        .setContentText(message)
                        .setSmallIcon(R.mipmap.ic_timer) // Make sure this icon exists!
                        .setAutoCancel(true)
                        .build()

        notificationManager.notify(1, notification)

        // Cancel after 3 seconds
        android.os.Handler(mainLooper).postDelayed({ notificationManager.cancel(1) }, 3000)
    }
}
