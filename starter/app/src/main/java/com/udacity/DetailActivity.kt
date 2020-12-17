package com.udacity

import android.app.DownloadManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)
        setSupportActionBar(toolbar)

        val binding = detailBinding.contentDetail

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val downloadInfo = intent.getParcelableExtra<DownloadInfo>(getString(R.string.download_info_intent_name))
        binding.downloadFilename.text = downloadInfo?.fileName
        binding.downloadStatus.text = downloadInfo?.status

        if (downloadInfo?.status == getString(R.string.failed))
            binding.downloadStatus.setTextColor(Color.RED)

        Log.i("DetailActivity","downloadInfo: $downloadInfo")

        binding.buttonOk.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
