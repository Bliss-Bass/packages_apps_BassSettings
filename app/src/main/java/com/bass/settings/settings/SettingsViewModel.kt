package com.bass.settings.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed interface DisplayableItem
data class CategoryItem(val name: String) : DisplayableItem
data class SettingItem(val setting: Setting) : DisplayableItem

// A new event class to communicate the result of a setting update to the UI.
data class SettingUpdateResult(val setting: Setting, val success: Boolean)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val displayableItems: List<DisplayableItem>

    private val _updateResultEvent = MutableSharedFlow<SettingUpdateResult>()
    val updateResultEvent = _updateResultEvent.asSharedFlow()

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
        viewModelScope.launch {
            val app = getApplication<Application>()
            val success = SettingsManager.setSetting(
                app.contentResolver,
                setting.type,
                setting.id,
                value,
                setting.defaultValue
            )

            if (success) {
                setting.value = value
            }

            _updateResultEvent.emit(SettingUpdateResult(setting, success))
        }
    }
}
