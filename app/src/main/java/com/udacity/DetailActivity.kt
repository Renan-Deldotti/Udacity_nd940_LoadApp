package com.udacity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList

class DetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DetailActivity"
        const val REPO_KEY: String = "DetailActivity:REPO_KEY"
        const val REPO_DOWNLOAD_SUCCESS = "DetailActivity:REPO_DOWNLOAD_SUCCESS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // Get the data from the Intent
        val downloadedRepo = intent.getStringExtra(REPO_KEY)
        val downloadSucceeded = intent.getBooleanExtra(REPO_DOWNLOAD_SUCCESS, false)
        val repoName = downloadedRepo?.split("/")?.get(3)

        // Populate the fieds with the received data
        downloaded_file_name_tV.text  = when(repoName) {
            "udacity" -> {
                getString(R.string.loadapp_download_description)
            }
            "bumptech" -> {
                getString(R.string.glide_download_description)
            }
            "square" -> {
                getString(R.string.retrofit_download_description)
            }
            else -> {
                repoName
            }
        }
        if (downloadSucceeded) {
            downloaded_file_status.text = getString(R.string.success)
            downloaded_file_status.setTextColor(getColor(R.color.status_success))
        } else {
            downloaded_file_status.text = getString(R.string.fail)
            downloaded_file_status.setTextColor(getColor(R.color.status_fail))
        }

        // Finish and go to the MainActivity
        ok_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
