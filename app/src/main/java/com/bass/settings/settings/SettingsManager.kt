package com.bass.settings.settings

import android.content.ContentResolver
import android.provider.Settings

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

    fun getSetting(contentResolver: ContentResolver, type: SettingType, key: String, defaultValue: Any): Any {
        val valueStr: String? = when (type) {
            SettingType.SECURE -> Settings.Secure.getString(contentResolver, key)
            SettingType.SYSTEM -> Settings.System.getString(contentResolver, key)
            SettingType.GLOBAL -> Settings.Global.getString(contentResolver, key)
            SettingType.SYSTEM_PROPERTY -> getSystemProperty?.invoke(null, key, null) as? String
        }

        // Instead of relying on the parsed type of defaultValue, which can be a String,
        // determine the intended type from the content of the defaultValue.
        val isBoolean = defaultValue.toString().equals("true", ignoreCase = true) || defaultValue.toString().equals("false", ignoreCase = true)

        val actualDefaultValue = if (isBoolean) {
            defaultValue.toString().toBoolean()
        } else {
            defaultValue.toString().toIntOrNull() ?: 0
        }

        if (valueStr.isNullOrEmpty()) {
            return actualDefaultValue
        }

        // Return a value of the same type as our parsed default value
        return if (isBoolean) {
            valueStr == "1" || valueStr.equals("true", ignoreCase = true)
        } else {
            valueStr.toIntOrNull() ?: actualDefaultValue
        }
    }

    fun setSetting(contentResolver: ContentResolver, type: SettingType, key: String, value: Any, defaultValue: Any): Boolean {
        val valueStr = when (value) {
            is Boolean -> if (value) "1" else "0"
            else -> value.toString()
        }

        try {
            when (type) {
                SettingType.SECURE -> Settings.Secure.putString(contentResolver, key, valueStr)
                SettingType.SYSTEM -> Settings.System.putString(contentResolver, key, valueStr)
                SettingType.GLOBAL -> Settings.Global.putString(contentResolver, key, valueStr)
                SettingType.SYSTEM_PROPERTY -> setSystemProperty?.invoke(null, key, valueStr)
            }
        } catch (e: SecurityException) {
            // If the set fails due to a security exception, we know it failed.
            return false
        }

        // After setting, read the value back to verify it was set correctly.
        val readBackValue = getSetting(contentResolver, type, key, defaultValue)
        return value == readBackValue
    }
}
