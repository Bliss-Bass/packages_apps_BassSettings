package com.bass.settings.settings

import android.content.Context
import com.bass.settings.R
import org.xmlpull.v1.XmlPullParser

class SettingsParser(private val context: Context) {

    fun parse(): List<Setting> {
        val settings = mutableListOf<Setting>()
        val parser = context.resources.getXml(R.xml.bass_settings)

        var category: SettingCategory? = null

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "category" -> {
                            val categoryName = parser.getAttributeValue(null, "name")
                            category = SettingCategory.valueOf(categoryName.uppercase())
                        }
                        "setting" -> {
                            val id = parser.getAttributeValue(null, "id")
                            val nameResId = parser.getAttributeResourceValue(null, "name", 0)
                            val descriptionResId = parser.getAttributeResourceValue(null, "description", 0)
                            val typeName = parser.getAttributeValue(null, "type")
                            val type = SettingType.valueOf(typeName.uppercase())
                            val defaultValueStr = parser.getAttributeValue(null, "defaultValue")

                            val name = if (nameResId != 0) context.getString(nameResId) else ""
                            val description = if (descriptionResId != 0) context.getString(descriptionResId) else null

                            val defaultValue = when {
                                defaultValueStr.equals("true", true) -> true
                                defaultValueStr.equals("false", true) -> false
                                else -> defaultValueStr.toIntOrNull() ?: defaultValueStr
                            }

                            settings.add(
                                Setting(
                                    id = id,
                                    name = name,
                                    description = description,
                                    type = type,
                                    defaultValue = defaultValue,
                                    category = category!!
                                )
                            )
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "category") {
                        category = null
                    }
                }
            }
            parser.next()
        }

        return settings
    }
}