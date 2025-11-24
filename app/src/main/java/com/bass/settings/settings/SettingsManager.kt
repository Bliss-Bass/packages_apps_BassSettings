package com.bass.settings.settings

import android.content.ContentResolver
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SettingsManager {

    private val systemPropertiesClass by lazy {
        try {
            Class.forName("android.os.SystemProperties")
        } catch (e: Exception) {
            null
        }
    }

    private val getSystemProperty by lazy {
        systemPropertiesClass?.getMethod("get", String::class.java, String::class.java)
    }

    private val setSystemProperty by lazy {
        systemPropertiesClass?.getMethod("set", String::class.java, String::class.java)
    }

    /**
     * Gets the value of a setting, always returning a Boolean.
     * It interprets "1" or "true" as true, and anything else (including null) as false.
     */
    fun getSetting(contentResolver: ContentResolver, type: SettingType, key: String): Boolean {
        val valueStr: String? = when (type) {
            SettingType.SECURE -> Settings.Secure.getString(contentResolver, key)
            SettingType.SYSTEM -> Settings.System.getString(contentResolver, key)
            SettingType.GLOBAL -> Settings.Global.getString(contentResolver, key)
            SettingType.SYSTEM_PROPERTY -> getSystemProperty?.invoke(null, key, null) as? String
        }
        return valueStr == "1" || valueStr.equals("true", ignoreCase = true)
    }

    /**
     * Asynchronously sets the value of a setting and verifies that the write was successful.
     */
    suspend fun setSetting(contentResolver: ContentResolver, type: SettingType, key: String, value: Boolean): Boolean = withContext(Dispatchers.IO) {
        val valueStr = if (value) "1" else "0"

        try {
            when (type) {
                SettingType.SECURE -> Settings.Secure.putString(contentResolver, key, valueStr)
                SettingType.SYSTEM -> Settings.System.putString(contentResolver, key, valueStr)
                SettingType.GLOBAL -> Settings.Global.putString(contentResolver, key, valueStr)
                SettingType.SYSTEM_PROPERTY -> setSystemProperty?.invoke(null, key, valueStr)
            }
        } catch (e: SecurityException) {
            return@withContext false
        }

        // After setting, read the value back to verify it was set correctly.
        val readBackValue = getSetting(contentResolver, type, key)
        return@withContext value == readBackValue
    }
}
