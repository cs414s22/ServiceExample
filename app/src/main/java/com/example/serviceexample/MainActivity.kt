package com.example.serviceexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    // to keep track whether the service is running
    private var serviceIsRunning = false

    // Pretend these files are urls that the user wants to download
    private val listOfFakeFileNames = arrayListOf("File-1", "File-2", "File-3", "File-4", "File-5",
        "File-6", "File-7", "File-8", "File-9", "File-10")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startServiceButton(view: View) {

        if (serviceIsRunning) {
            Log.d(TAG, "Service is currently running, please wait until it is done")
            return
        }

        // Register the broadcast receiver
        registerMyReceiver()

        val intent = Intent(this, MyService::class.java)
        intent.putStringArrayListExtra("filesToDownload", listOfFakeFileNames)
        startService(intent) // not startActivity


        // Show a static text
        findViewById<TextView>(R.id.tv_downloading).text = "Downloading..."
        serviceIsRunning = true
    }


    private inner class DownloadReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            // Handle the received broadcast message

            val file = intent.getStringExtra("file")
            Log.d(TAG, "DownloadReceiver: $file has been downloaded")

            // Update the UI, by running the code below in UI Thread
            runOnUiThread {

                val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
                progressBar.progress = progressBar.progress + 10

                val progress = progressBar.progress / listOfFakeFileNames.size

                if (progress == listOfFakeFileNames.size){
                    findViewById<TextView>(R.id.tv_downloading).text = "Download has completed!"
                } else {
                    // This will look like : Downloading ... 1/10 etc.
                    findViewById<TextView>(R.id.tv_downloading).text = "Downloading... $progress / ${listOfFakeFileNames.size}"
                }


            }

        }
    }

    private fun registerMyReceiver() {
        // Register a receiver to know the service is done
        val filter = IntentFilter()
        filter.addAction("downloadComplete")
        registerReceiver(DownloadReceiver(), filter)
    }

    //  unregister the broadcast receiver after this activity is paused
    override fun onPause() {
        super.onPause()
        //unregisterReceiver(DownloadReceiver())
        //serviceIsRunning = false
    }



}