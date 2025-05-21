package com.loyalstring.rfid.data.reader

import android.content.Context
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory
import com.rscja.barcode.BarcodeUtility
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _barcodeDecoder: BarcodeDecoder = BarcodeFactory.getInstance().barcodeDecoder

    val barcodeDecoder: BarcodeDecoder
        get() = _barcodeDecoder

    init {
        _barcodeDecoder.open(context)
    }

    fun setOnBarcodeScanned(callback: (String) -> Unit) {

        barcodeDecoder.setDecodeCallback { entity ->
            if (entity.resultCode == BarcodeDecoder.DECODE_SUCCESS) {
                BarcodeUtility.getInstance().enablePlaySuccessSound(context, true) //success Sound
                callback(entity.barcodeData)
            }
        }
    }

}
