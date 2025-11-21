package com.bass.settings.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val settings: List<Setting>

    init {
        val app = getApplication<Application>()
        val parser = SettingsParser(app)
        settings = parser.parse().onEach { setting ->
            setting.value = SettingsManager.getSetting(app.contentResolver, setting.type, setting.id, setting.defaultValue)
        }
    }

    fun updateSetting(setting: Setting, value: Any) {
        val app = getApplication<Application>()
        SettingsManager.setSetting(app.contentResolver, setting.type, setting.id, value)
        setting.value = value
    }
}
