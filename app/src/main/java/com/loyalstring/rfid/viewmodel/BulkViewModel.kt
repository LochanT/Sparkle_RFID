package com.loyalstring.rfid.viewmodel

import android.R.attr.tag
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyalstring.rfid.data.reader.RFIDReaderManager
import com.rscja.deviceapi.entity.UHFTAGInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BulkViewModel @Inject constructor(
    private val readerManager: RFIDReaderManager,
) : ViewModel() {

        val success = readerManager.initReader()


    private val _scannedTags = MutableStateFlow<List<UHFTAGInfo>>(emptyList())
    val scannedTags: StateFlow<List<UHFTAGInfo>> = _scannedTags

    private var scanJob: Job? = null


    fun startScanning() {
        if (success){
            readerManager.startInventoryTag()
            if (scanJob?.isActive == true) return

            scanJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    val tag = readerManager.readTagFromBuffer()
                    if (tag != null) {
                        Log.e("RFID", "Tag read: $tag")
                        _scannedTags.update { currentList ->
                            if (currentList.any { it.epc == tag.epc }) currentList
                            else currentList + tag


                        }
                    } else {
                        Log.e("RFID", "No tag in buffer")
                    }

                }
            }
        }

    }

    fun stopScanning() {
        scanJob?.cancel()
        readerManager.stopInventory()

    }

    fun resetData(){
        _scannedTags.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}