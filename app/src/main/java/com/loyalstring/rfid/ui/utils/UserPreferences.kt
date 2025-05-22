package com.loyalstring.rfid.ui.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class UserPreferences(context: Context) {

    companion object {
        private const val PREF_NAME = "user_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_EMPLOYEE = "employee"
        // private const val KEY_USERNAME = "username"

        private val gson = Gson()

    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }


    fun <T> saveEmployee(employee: T) {
        val json = gson.toJson(employee)
        prefs.edit().putString(KEY_EMPLOYEE, json).apply()
    }

    fun <T> getEmployee(clazz: Class<T>): T? {
        val json = prefs.getString(KEY_EMPLOYEE, null)
        return if (json != null) gson.fromJson(json, clazz) else null
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}