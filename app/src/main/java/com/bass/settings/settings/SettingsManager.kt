package com.bass.settings.settings

import android.content.ContentResolver
import android.provider.Settings
import java.lang.reflect.Method

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
        return when (type) {
            SettingType.SECURE -> Settings.Secure.getString(contentResolver, key) ?: defaultValue
            SettingType.SYSTEM -> Settings.System.getString(contentResolver, key) ?: defaultValue
            SettingType.GLOBAL -> Settings.Global.getString(contentResolver, key) ?: defaultValue
            SettingType.SYSTEM_PROPERTY -> {
                val value = getSystemProperty?.invoke(null, key, defaultValue.toString()) as? String
                when (defaultValue) {
                    is Boolean -> value?.toBoolean() ?: defaultValue
                    is Int -> value?.toIntOrNull() ?: defaultValue
                    else -> value ?: defaultValue
                }
            }
        }
    }

    fun setSetting(contentResolver: ContentResolver, type: SettingType, key: String, value: Any) {
        when (type) {
            SettingType.SECURE -> Settings.Secure.putString(contentResolver, key, value.toString())
            SettingType.SYSTEM -> Settings.System.putString(contentResolver, key, value.toString())
            SettingType.GLOBAL -> Settings.Global.putString(contentResolver, key, value.toString())
            SettingType.SYSTEM_PROPERTY -> setSystemProperty?.invoke(null, key, value.toString())
        }
    }
}
