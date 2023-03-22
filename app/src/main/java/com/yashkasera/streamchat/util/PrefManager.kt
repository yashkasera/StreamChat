package com.yashkasera.streamchat.util

import android.content.Context
import android.content.SharedPreferences
import com.yashkasera.streamchat.AppObjectController

object PrefManager {
    private val prefs: SharedPreferences by lazy {
        AppObjectController.streamChatApplication.getSharedPreferences(
            AppObjectController.streamChatApplication.packageName,
            Context.MODE_PRIVATE
        )
    }

    fun <T> put(key: String, value: T) {
        when (value) {
            is Int -> prefs.edit().putInt(key, value).apply()
            is String -> prefs.edit().putString(key, value).apply()
            is Boolean -> prefs.edit().putBoolean(key, value).apply()
            is Float -> prefs.edit().putFloat(key, value).apply()
            is Long -> prefs.edit().putLong(key, value).apply()
            else -> AppObjectController.gson.toJson(value)?.let { put(key, it) }
        }
    }

    fun getString(key: String, defValue: String? = null) = prefs.getString(key, defValue ?: EMPTY) ?: defValue ?: EMPTY

    fun getInt(key: String, defValue: Int? = null) = prefs.getInt(key, defValue ?: -1)

    fun getBoolean(key: String, defValue: Boolean? = null) = prefs.getBoolean(key, defValue ?: false)

    fun getFloat(key: String, defValue: Float? = null) = prefs.getFloat(key, defValue ?: 0f)

    fun getLong(key: String, defValue: Long? = null) = prefs.getLong(key, defValue ?: 0L)

    fun clear() = prefs.edit().clear().apply()

    fun <T> getObject(key: String, classOfT: Class<T>): T? {
        val json = getString(key)
        return AppObjectController.gson.fromJson(json, classOfT)
    }

}