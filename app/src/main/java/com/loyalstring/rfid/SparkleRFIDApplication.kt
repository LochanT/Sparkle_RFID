package com.loyalstring.rfid

import android.app.Application
import android.util.Log
import com.rscja.deviceapi.RFIDWithUHFUART
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SparkleRFIDApplication : Application() {
    var mReader: RFIDWithUHFUART? = null
    init {
        try {
            mReader = RFIDWithUHFUART.getInstance()
        } catch (ex: Exception) {
            println("exception : $ex")
        }

        mReader?.init()
    }
}
