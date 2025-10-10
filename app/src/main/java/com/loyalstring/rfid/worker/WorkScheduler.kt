package com.loyalstring.rfid.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun schedulePeriodicSync(context: Context, intervalMinutes: Long) {
    val request = PeriodicWorkRequestBuilder<SyncDataWorker>(
        intervalMinutes.coerceAtLeast(intervalMinutes), // min 15min
        TimeUnit.MINUTES
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
        SyncDataWorker.SYNC_DATA_WORKER,
        ExistingPeriodicWorkPolicy.REPLACE,
        request
    )
    Log.e("SYNC_DATA", "Called")
}

// ✅ cancels worker if user turns off auto-sync
fun cancelPeriodicSync(context: Context) {
    WorkManager.getInstance(context.applicationContext)
        .cancelUniqueWork(SyncDataWorker.SYNC_DATA_WORKER)
}
