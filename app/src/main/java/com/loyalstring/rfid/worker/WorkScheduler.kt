package com.loyalstring.rfid.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun schedulePeriodicSync(context: Context, intervalMinutes: Long) {
    val request = PeriodicWorkRequestBuilder<SyncDataWorker>(
        intervalMinutes.coerceAtLeast(15), // min 15min
        TimeUnit.MINUTES
    )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        SyncDataWorker.SYNC_DATA_WORKER,
        ExistingPeriodicWorkPolicy.REPLACE,
        request
    )
}

// ✅ cancels worker if user turns off auto-sync
fun cancelPeriodicSync(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(SyncDataWorker.SYNC_DATA_WORKER)
}
