package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
//    private lateinit var pendingIntent: PendingIntent
//    private lateinit var action: NotificationCompat.Action

    private var job: Job? = null
    private lateinit var binding: ContentMainBinding

    private var isDownloading = false
    private var url = ""
    private var filename = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        setSupportActionBar(toolbar)

        binding = mainBinding.contentMain
        url = ""

        notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name)
        )

        binding.customButton.setOnClickListener {
            if (url.isEmpty() && binding.customDownload.text.toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.warning_none_selected), Toast.LENGTH_LONG).show()
//                binding.customButton.updateProgress(100)
                binding.customButton.complete() // Return button to Completed ButtonState
            } else if (!isDownloading)
                download()
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            when (radioGroup.checkedRadioButtonId) {
                R.id.rb_glide -> urlAndFilenameSign(URL_GLIDE, getString(R.string.glide_radio_button))
                R.id.rb_loadapp -> urlAndFilenameSign(URL_LOADAPP, getString(R.string.loadapp_radio_button))
                R.id.rb_retrofit -> urlAndFilenameSign(URL_RETROFIT, getString(R.string.retrofit_radio_button))
            }
        }

        // Clear radio group when user type
        binding.customDownload.doAfterTextChanged {
            if (!it.isNullOrEmpty())
                radioGroup.clearCheck()
        }
    }

    private fun urlAndFilenameSign(url: String, filename: String) {
        this.url = url
        this.filename = filename
        binding.customDownload.setText("") // Clear Custom Link
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            var status = ""

            // To get the status of downloads
            val query = DownloadManager.Query().setFilterById(id!!)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.query(query).use { cursor ->
                if (cursor.moveToFirst()) {
                    val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    status = if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL)
                        getString(R.string.success)
                    else
                        getString(R.string.failed)

                    // Get filename of user custom link
                    if (binding.customDownload.text.toString().isNotEmpty())
                        filename = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                }
            }

            val downloadInfo = DownloadInfo(filename, status)
            Log.i("MainActivity", ">>> downloadInfo: $downloadInfo")
            context?.let {
                notificationManager.sendNotification(
                        it.getString(R.string.notification_description),
                        it,
                        downloadInfo
                )
            }
        }
    }

    // Create channel for notification
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = getString(R.string.notification_description)
            }

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun download() {
        job?.cancel()

        // Check for user custom link
        val input = binding.customDownload.text.toString()
        if (input.isNotEmpty()) {
            if (URLUtil.isValidUrl(input)) {
                url = input
                binding.textInputCustomDownload.error = null
            } else {
                binding.textInputCustomDownload.error = "Link is not valid"
                binding.customButton.complete() // Return the button state to Completed Button State
                return
            }
        } else
            binding.textInputCustomDownload.error = null

        notificationManager.cancelAll() // Cancel All notification
        val request =
                DownloadManager.Request(Uri.parse(url))
                        .setDescription(getString(R.string.app_description))
                        .setRequiresCharging(false)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        // To look at the progress of download in real time
        val query = DownloadManager.Query().setFilterById(downloadID)
        var totalBytes = 0
        isDownloading = true
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isDownloading) {
                downloadManager.query(query).use { cursor ->
                    if (cursor.moveToFirst()) {
                        if (totalBytes <= 0)
                            totalBytes =
                                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (totalBytes == 0)
                            totalBytes = -1

                        val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        val byteDownloadedSoFar = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val percentProgress = (byteDownloadedSoFar * LoadingButton.PERCENTAGE_MAX) / totalBytes

                        // Update LoadingButton
                        binding.customButton.updateProgress(percentProgress)

                        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL || downloadStatus == DownloadManager.STATUS_FAILED) {
                            binding.customButton.complete() // To complete the state of Button
                            isDownloading = false // Finish the loop
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    companion object {
        private const val URL_LOADAPP =
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"

        private const val URL_RETROFIT =
                "https://codeload.github.com/square/retrofit/zip/master"

        private const val URL_GLIDE =
                "https://codeload.github.com/bumptech/glide/zip/master"
    }
}
