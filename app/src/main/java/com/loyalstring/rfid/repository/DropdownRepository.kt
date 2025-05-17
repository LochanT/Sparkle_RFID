package com.loyalstring.rfid.repository

import com.loyalstring.rfid.data.local.dao.DropdownDao
import com.loyalstring.rfid.data.local.entity.Category
import com.loyalstring.rfid.data.local.entity.Design
import com.loyalstring.rfid.data.local.entity.Product
import javax.inject.Inject

class DropdownRepository @Inject constructor(private val dao: DropdownDao) {
    val categories = dao.getAllCategories()
    val products = dao.getAllProducts()
    val designs = dao.getAllDesigns()

    suspend fun addCategory(name: String) = dao.insertCategory(Category(name = name))
    suspend fun addProduct(name: String) = dao.insertProduct(Product(name = name))
    suspend fun addDesign(name: String) = dao.insertDesign(Design(name = name))
}
