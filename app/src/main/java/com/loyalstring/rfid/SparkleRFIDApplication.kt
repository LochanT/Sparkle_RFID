package com.loyalstring.rfid

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.rscja.deviceapi.RFIDWithUHFUART
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SparkleRFIDApplication : Application() {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    var mReader: RFIDWithUHFUART? = null

    override fun onCreate() {
        super.onCreate()

        // Ensure Hilt injected WorkerFactory is ready before WorkManager starts
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

        WorkManager.initialize(this, config)
        Log.d("WORKER_INIT", "WorkManager initialized with HiltWorkerFactory = $workerFactory")

        // ✅ Continue with your RFID initialization
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = RFIDWithUHFUART.getInstance()
                if (reader != null && reader.init()) {
                    mReader = reader
                    Log.d("SparkleRFID", "✅ RFID Reader initialized successfully")
                } else {
                    Log.e("SparkleRFID", "❌ Failed to initialize RFID Reader")
                }
            } catch (ex: Exception) {
                Log.e("SparkleRFID", "⚠ Exception initializing RFID: ${ex.message}")
            }
        }
    }


    // ✅ Use property override (required for WorkManager 2.8+)


}
