package com.loyalstring.rfid.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.login.Employee
import com.loyalstring.rfid.repository.BulkRepository
import com.loyalstring.rfid.ui.utils.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker

class SyncDataWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val repo: BulkRepository,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            repo.syncBulkItemsFromServer(
                ClientCodeRequest(userPreferences.getEmployee(Employee::class.java)?.clientCode)
            )
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val SYNC_DATA_WORKER = "sync_data_worker"
    }

}
