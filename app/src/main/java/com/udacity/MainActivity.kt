package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val UDACITY_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GLIDE_URL =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val RETROFIT_URL =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
        private const val CHANNEL_NAME = "channelName"
        private const val CHANNEL_DESCRIPTION = "channelDescription"
        private const val NOTIFICATION_ID = 1001
        private const val MAIN_REQUEST_CODE_DETAIL = 2001
    }

    private var downloadID: Long = 0
    private var urlFromRadioGroup: String = ""
    private var lastDownloadedRepo:String = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.setTitle(R.string.toolbar_app_name)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Get the user choice
        download_options_rG.setOnCheckedChangeListener { group, id ->
            when (id) {
                R.id.download_loadapp_button -> {
                    urlFromRadioGroup = UDACITY_URL
                }
                R.id.download_retrofit_button -> {
                    urlFromRadioGroup = RETROFIT_URL
                }
                R.id.download_glide_button -> {
                    urlFromRadioGroup = GLIDE_URL
                }
            }
        }

        // We should only start to download if there is an internet connection and a selected repo
        custom_button.setOnClickListener {
            if (!hasInternetConnection()) {
                Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            } else if (urlFromRadioGroup.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_repo_selected), Toast.LENGTH_SHORT).show()
            } else {
                download()
            }
        }

        createNotificationChannel()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // Change the behavior and show the notification
            custom_button.buttonState = ButtonState.Completed
            sendDownloadNotification(downloadID == id)
        }
    }

    private fun download() {
        lastDownloadedRepo = urlFromRadioGroup

        val request =
            DownloadManager.Request(Uri.parse(lastDownloadedRepo))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        custom_button.buttonState = ButtonState.Loading

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun hasInternetConnection(): Boolean {
        // Check if there is an internet connection active
        val connectivityManager: ConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    private fun createNotificationChannel() {
        // Notification channel is only needed on Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Creates the channel
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.setShowBadge(false)
            notificationChannel.enableVibration(true)
            notificationChannel.description = CHANNEL_DESCRIPTION

            // Register the channel
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun sendDownloadNotification(success: Boolean) {
        // Creates the intent to open the DetailActivity
        val detailActivityIntent = Intent(this, DetailActivity::class.java)

        // Add some info about the download
        val infoBundle = Bundle()
        infoBundle.putString(DetailActivity.REPO_KEY, lastDownloadedRepo)
        infoBundle.putBoolean(DetailActivity.REPO_DOWNLOAD_SUCCESS, success)
        detailActivityIntent.putExtras(infoBundle)

        // Create the PendingIntent
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, MAIN_REQUEST_CODE_DETAIL, detailActivityIntent, flags)

        // Creates the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_assistant_black_24dp, getString(R.string.notification_button), pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }
}
