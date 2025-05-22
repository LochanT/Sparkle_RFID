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

        private const val KEY_USERNAME = "remember_username"
        private const val KEY_PASSWORD = "remember_password"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_LOGGED_IN = "logged_in"

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

    fun saveLoginCredentials(username: String, password: String, rememberMe: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            if (rememberMe) {
                putString(KEY_USERNAME, username)
                putString(KEY_PASSWORD, password)
            } else {
                remove(KEY_USERNAME)
                remove(KEY_PASSWORD)
            }
            apply()
        }
    }

    fun getSavedUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getSavedPassword(): String = prefs.getString(KEY_PASSWORD, "") ?: ""
    fun isRememberMe(): Boolean = prefs.getBoolean(KEY_REMEMBER_ME, false)

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
