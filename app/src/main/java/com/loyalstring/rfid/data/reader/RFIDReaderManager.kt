package com.loyalstring.rfid.data.reader


import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import com.loyalstring.rfid.R
import com.loyalstring.rfid.data.model.ScannedItem
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
        initSounds()
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

    fun startInventoryTag(selectedPower: Int): Boolean {
        reader?.setPower(selectedPower)
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
        soundMap[2] = soundPool!!.load(context, R.raw.sixty, 1)
        soundMap[3] = soundPool!!.load(context, R.raw.seventy, 1)
        soundMap[4] = soundPool!!.load(context, R.raw.fourty, 1)
        soundMap[5] = soundPool!!.load(context, R.raw.found1, 1)
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

    fun stopSound(id: Int) {

        soundPool!!.stop(id) // Stop the sound using the stored stream ID
        // Remove the stream ID from the map

    }

    fun inventorySingleTag(selectedPower: Int): UHFTAGInfo? {
        if (reader == null || !reader.isInventorying) {
            Log.e("RFID", "Reader is null or not opened")
            initReader()
        }
        reader?.setPower(selectedPower)
        return reader?.inventorySingleTag()
    }

}
