package com.bass.settings.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel

sealed interface DisplayableItem
data class CategoryItem(val name: String) : DisplayableItem
data class SettingItem(val setting: Setting) : DisplayableItem

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val displayableItems: List<DisplayableItem>

    init {
        val app = getApplication<Application>()
        val parser = SettingsParser(app)
        val settings = parser.parse().onEach { setting ->
            setting.value = SettingsManager.getSetting(app.contentResolver, setting.type, setting.id, setting.defaultValue)
        }

        val groupedSettings = settings.groupBy { it.category }
        val items = mutableListOf<DisplayableItem>()
        groupedSettings.forEach { (category, settingsList) ->
            items.add(CategoryItem(category.name.replaceFirstChar { it.titlecase() }))
            items.addAll(settingsList.map { SettingItem(it) })
        }
        displayableItems = items
    }

    fun updateSetting(setting: Setting, value: Any) {
        val app = getApplication<Application>()
        SettingsManager.setSetting(app.contentResolver, setting.type, setting.id, value)
        setting.value = value
    }
}
