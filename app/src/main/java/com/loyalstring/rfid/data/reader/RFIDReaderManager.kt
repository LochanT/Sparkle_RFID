package com.loyalstring.rfid.data.reader


import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import com.loyalstring.rfid.R
import com.rscja.custom.UHFCSYX
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RFIDReaderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    var soundMap: HashMap<Int?, Int?> = HashMap<Int?, Int?>()
    private var soundPool: SoundPool? = null
    private var volumnRatio = 0f
    private var am: AudioManager? = null
    private val reader: RFIDWithUHFUART? = RFIDWithUHFUART.getInstance()

    fun initReader(): Boolean {
        val success = reader?.init() ?: false
        if (success) {
            Log.d("RFID", "Reader initialized successfully")
        } else {
            Log.e("RFID", "Reader initialization failed")
        }
        return success
    }

    fun readTagFromBuffer(): UHFTAGInfo? {
        return reader?.readTagFromBuffer()
    }

    fun startInventoryTag(): Boolean {
        val started = reader?.startInventoryTag() ?: false
        Log.d("RFID", "startInventoryTag: $started")
        return started
    }

    fun stopInventory() {
        reader?.stopInventory()
        Log.d("RFID", "Inventory stopped")
    }

    fun release() {
        reader?.free()
    }

    fun initSounds() {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        soundMap[1] = soundPool?.load(context, R.raw.barcodebeep, 1)
        soundMap[2] = soundPool?.load(context, R.raw.sixty, 1)
        // ... add others
        am = context.getSystemService(AUDIO_SERVICE) as AudioManager
    }

    fun playSound(type: Int = 1) {
        val maxVolume = am?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
        val currentVolume = am?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 1f
        volumnRatio = currentVolume / maxVolume
        soundMap[type]?.let {
            soundPool?.play(it, volumnRatio, volumnRatio, 1, 0, 1f)
        }
    }

    fun playSound(success: Boolean) {
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 5)
        soundPool?.let { soundMap.put(1, it.load(context, R.raw.barcodebeep, 1)) }
        soundPool?.let { soundMap.put(2, it.load(context, R.raw.sixty, 1)) }
        soundPool?.let { soundMap.put(3, it.load(context, R.raw.seventy, 1)) }
        soundPool?.let { soundMap.put(4, it.load(context, R.raw.fourty, 1)) }
        soundPool?.let { soundMap.put(5, it.load(context, R.raw.found1, 1)) }
        am = context.getSystemService(AUDIO_SERVICE) as AudioManager
    }
}
