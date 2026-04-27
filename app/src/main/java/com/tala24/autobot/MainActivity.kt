package com.tala24.autobot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // دکمه برای تست دستی یک بار
        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val req = OneTimeWorkRequestBuilder<AutoSendWorker>().build()
            WorkManager.getInstance(this).enqueue(req)
        }

        // زمان‌بندی خودکار هر ۲ ساعت
        schedulePeriodicWork()
    }

    private fun schedulePeriodicWork() {
        val request =
            PeriodicWorkRequestBuilder<AutoSendWorker>(2, TimeUnit.HOURS)
                .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "AutoSender",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }
}
