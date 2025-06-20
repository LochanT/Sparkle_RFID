package com.loyalstring.rfid.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loyalstring.rfid.data.model.ClientCodeRequest
import com.loyalstring.rfid.data.model.addSingleItem.CategoryModel
import com.loyalstring.rfid.data.model.addSingleItem.DesignModel
import com.loyalstring.rfid.data.model.addSingleItem.InsertProductRequest
import com.loyalstring.rfid.data.model.addSingleItem.ProductModel
import com.loyalstring.rfid.data.model.addSingleItem.PurityModel
import com.loyalstring.rfid.data.model.addSingleItem.SKUModel
import com.loyalstring.rfid.data.model.addSingleItem.VendorModel
import com.loyalstring.rfid.data.reader.BarcodeReader
import com.loyalstring.rfid.data.reader.RFIDReaderManager
import com.loyalstring.rfid.data.remote.resource.Resource
import com.loyalstring.rfid.repository.SingleProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingleProductViewModel @Inject constructor(
    private val repository: SingleProductRepository,
    private val readerManager: RFIDReaderManager,
    internal val barcodeReader: BarcodeReader,
) : ViewModel() {
    private val success = readerManager.initReader()
    private val _vendorResponse = MutableLiveData<Resource<List<VendorModel>>>()
    val vendorResponse: LiveData<Resource<List<VendorModel>>> = _vendorResponse


    private val _skuResponse = MutableLiveData<Resource<List<SKUModel>>>()
    val skuResponse: LiveData<Resource<List<SKUModel>>> = _skuResponse

    private val _categoryResponse = MutableLiveData<Resource<List<CategoryModel>>>()
    val categoryResponse: LiveData<Resource<List<CategoryModel>>> = _categoryResponse

    private val _productresponse = MutableLiveData<Resource<List<ProductModel>>>()
    val productResponse: LiveData<Resource<List<ProductModel>>> = _productresponse

    private val _designResponse = MutableLiveData<Resource<List<DesignModel>>>()
    val designResponse: LiveData<Resource<List<DesignModel>>> = _designResponse

    private val _purityResponse = MutableLiveData<Resource<List<PurityModel>>>()
    val purityResponse: LiveData<Resource<List<PurityModel>>> = _purityResponse

    var stockResponse by mutableStateOf<Result<List<PurityModel>>?>(null)
        private set

    /*venodr function*/
    fun getAllVendor(request: ClientCodeRequest) {
        viewModelScope.launch {
            _vendorResponse.value = Resource.Loading()
            try {
                val response = repository.getAllVendorDetails(request)
                if (response.isSuccessful && response.body() != null) {
                    _vendorResponse.value = Resource.Success((response.body()!!))

                    Log.d("SingleProductViewModel", "Vendor" + response.body());
                } else {
                    _vendorResponse.value =
                        Resource.Error("Vendor fetch failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _vendorResponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    /*sku function*/
    fun getAllSKU(request: ClientCodeRequest) {
        viewModelScope.launch {
            _skuResponse.value = Resource.Loading()
            try {
                val response = repository.getAllSKUDetails(request)
                if (response.isSuccessful && response.body() != null) {
                    _skuResponse.value = Resource.Success((response.body()!!))

                    Log.d("SingleProductViewModel", "SKU" + response.body());
                } else {
                    _skuResponse.value = Resource.Error("sku fetch failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _skuResponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    /*catogory function*/
    fun getAllCategory(request: ClientCodeRequest) {
        viewModelScope.launch {
            _categoryResponse.value = Resource.Loading()
            try {
                val response = repository.getAllCategoryDetails(request)
                if (response.isSuccessful && response.body() != null) {
                    _categoryResponse.value = Resource.Success((response.body()!!))

                    Log.d("SingleProductViewModel", "Category" + response.body());
                } else {
                    _categoryResponse.value = Resource.Error("sku fetch failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _categoryResponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    /*product function*/
    fun getAllProduct(request: ClientCodeRequest) {
        viewModelScope.launch {
            _productresponse.value = Resource.Loading()
            try {
                val response = repository.getAllProductDetails(request)
                if (response.isSuccessful && response.body() != null) {
                    _productresponse.value = Resource.Success((response.body()!!))

                    Log.d("SingleProductViewModel", "Product" + response.body());
                } else {
                    _productresponse.value = Resource.Error("sku fetch failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _productresponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    /*product function*/
    fun getAllDesign(request: ClientCodeRequest) {
        viewModelScope.launch {
            _designResponse.value = Resource.Loading()
            try {
                val response = repository.getAllDesignDetails(request)
                if (response.isSuccessful && response.body() != null) {
                    _designResponse.value = Resource.Success((response.body()!!))

                    Log.d("SingleProductViewModel", "Product" + response.body());
                } else {
                    _designResponse.value = Resource.Error("sku fetch failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _designResponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    /*purity function*/
    fun getAllPurity(request: ClientCodeRequest) {
        viewModelScope.launch {
            _purityResponse.value = Resource.Loading()
            try {
                val response = repository.getAllPurityDetails(request)
                if (response.isSuccessful && response.body() != null) {
                    _purityResponse.value = Resource.Success((response.body()!!))

                    Log.d("SingleProductViewModel", "Product" + response.body());
                } else {
                    _purityResponse.value = Resource.Error("sku fetch failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _purityResponse.value = Resource.Error("Exception: ${e.message}")
            }
        }
    }

    fun saveImageUriToDb(uri: String) {
        viewModelScope.launch {
            val image = UploadedImage(uri = uri)
            //  dao.insert(image)
        }
    }


    fun fetchAllDropdownData(request: ClientCodeRequest) {
        viewModelScope.launch {
            launch { getAllVendor(request) }
            launch { getAllSKU(request) }
            launch { getAllCategory(request) }
            launch { getAllProduct(request) }
            launch { getAllDesign(request) }
            launch { getAllPurity(request) }
        }
    }


    fun insertLabelledStock(request: InsertProductRequest) {
        viewModelScope.launch {
            stockResponse = repository.insertLabelledStock(request)
        }
    }

    @Entity
    data class UploadedImage(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val uri: String
    )
}
