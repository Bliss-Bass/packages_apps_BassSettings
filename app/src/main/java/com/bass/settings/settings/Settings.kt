package com.bass.settings.settings

data class Setting(
    val id: String,
    val name: String,
    val description: String?,
    val type: SettingType,
    val defaultValue: Any,
    var value: Any = defaultValue,
    val category: SettingCategory
)

enum class SettingType {
    SECURE,
    SYSTEM,
    GLOBAL,
    SYSTEM_PROPERTY
}

enum class SettingCategory {
    BLISS,
    LAUNCHER,
    WINDOWMANAGER,
    OTHER
}
