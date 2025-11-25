package com.bass.settings.settings

// This file contains the data models for the settings feature.

data class Setting(
    val id: String,
    val name: String,
    val description: String?,
    val type: SettingType,
    val category: SettingCategory
)

enum class SettingType {
    SECURE,
    SYSTEM,
    GLOBAL,
    SYSTEM_PROPERTY
}

enum class SettingCategory {
    LAUNCHER,
    DISPLAY,
    WINDOWMANAGER,
    OTHER
}
