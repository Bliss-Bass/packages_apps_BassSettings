package com.bass.settings.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Defines the overall state of the settings screen
sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(val items: List<DisplayableItem>) : SettingsUiState
}

// A sealed interface for the items in our RecyclerView
sealed interface DisplayableItem {
    val key: String // Unique key for DiffUtil
}

data class CategoryItem(val name: String) : DisplayableItem {
    override val key: String get() = name
}

// A state holder for a single setting. This is immutable.
data class SettingItem(
    val setting: Setting, // The static definition from the parser
    val value: Boolean,      // The current value of the setting
    val isUpdating: Boolean  // True if a set operation is in progress
) : DisplayableItem {
    override val key: String get() = setting.id
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val parser = SettingsParser(app)
            val settingDefinitions = parser.parse()

            val settingItems = settingDefinitions.map { setting ->
                val currentValue = SettingsManager.getSetting(app.contentResolver, setting.type, setting.id)
                SettingItem(
                    setting = setting,
                    value = currentValue,
                    isUpdating = false
                )
            }

            val displayableItems = mutableListOf<DisplayableItem>()
            settingItems.groupBy { it.setting.category }.forEach { (category, settingsList) ->
                displayableItems.add(CategoryItem(category.name.replaceFirstChar { it.titlecase() }))
                displayableItems.addAll(settingsList)
            }

            _uiState.value = SettingsUiState.Success(displayableItems)
        }
    }

    fun updateSetting(settingId: String, newValue: Boolean) {
        viewModelScope.launch {
            // 1. Immediately update the UI to show the 'updating' state.
            _uiState.update {
                if (it !is SettingsUiState.Success) return@update it
                val updatedItems = it.items.map { item ->
                    if (item is SettingItem && item.setting.id == settingId) {
                        item.copy(isUpdating = true)
                    } else {
                        item
                    }
                }
                it.copy(items = updatedItems)
            }

            // 2. Perform the actual update in the background.
            val app = getApplication<Application>()
            val currentState = _uiState.value as? SettingsUiState.Success
            val settingItem = currentState?.items?.firstOrNull { it is SettingItem && it.setting.id == settingId } as? SettingItem

            if (settingItem != null) {
                val success = SettingsManager.setSetting(
                    app.contentResolver,
                    settingItem.setting.type,
                    settingItem.setting.id,
                    newValue
                )

                // 3. Update the UI with the final, confirmed state.
                val confirmedValue = if (success) newValue else !newValue // Revert UI on failure
                _uiState.update {
                    if (it !is SettingsUiState.Success) return@update it
                    val finalItems = it.items.map {
                        item ->
                        if (item is SettingItem && item.setting.id == settingId) {
                            item.copy(value = confirmedValue, isUpdating = false)
                        } else {
                            item
                        }
                    }
                    it.copy(items = finalItems)
                }
            }
        }
    }
}
