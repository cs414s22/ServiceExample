package com.example.serviceexample


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class MyService : Service() {

    private val TAG = "MyService"

    private val CHANNEL_ID = "com.example.notifications"

    init {
        Log.d(TAG, "Service is running...")
    }

    // We don't provide binding, so return null
    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Toast.makeText(this, "Service has started...", Toast.LENGTH_SHORT).show()
        //Log.d(TAG, "Thread-Id: ${Thread.currentThread().id} ")

        if (intent != null) {
            val filesToDownload = intent.getStringArrayListExtra("filesToDownload")

            if (!filesToDownload.isNullOrEmpty()) {

                Thread {
                    for (file in filesToDownload) {
                        Log.d(TAG, "Service is working: $file")
                        //Log.d(TAG, "Thread-Id: ${Thread.currentThread().id} ")
                        Thread.sleep(1000)


                        //Broadcast that the work is done!
                        val doneIntent = Intent()
                        doneIntent.action = "downloadComplete"
                        doneIntent.putExtra("file", file)
                        sendBroadcast(doneIntent)
                    }

                    // Stop the service as the service must be done at this point
                    stopSelf()

                    //Create a notification about the job is done
                    makeNotification()

                }.start()
            }
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }


    override fun onDestroy() {
        Toast.makeText(this, "Service is done", Toast.LENGTH_SHORT).show()
    }

    private fun makeNotification() {

        // Create a notification channel for new Android versions
        createNotificationChannel()

        // Pending intent to be able to launch the app when the user clicks the notification
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("file",  "test") // Optional, pass data

        val flag = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            else -> FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentTitle("Test")
            .setContentText("Download is complete")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        // Show the notification
        // notificationId is a unique int for each notification that you must define
        val notificationId = Random().nextInt()
        NotificationManagerCompat.from(this).notify(notificationId, builder.build())


    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My channel"
            val descriptionText = "My Default Priority Channel for Test"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}