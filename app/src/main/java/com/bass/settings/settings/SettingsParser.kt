package com.bass.settings.settings

import android.content.Context
import com.bass.settings.R
import org.xmlpull.v1.XmlPullParser

class SettingsParser(private val context: Context) {

    fun parse(): List<Setting> {
        val settings = mutableListOf<Setting>()
        val parser = context.resources.getXml(R.xml.bass_settings)

        var currentCategory: SettingCategory? = null

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "category" -> {
                            val categoryName = parser.getAttributeValue(null, "name")
                            currentCategory = categoryName?.let { 
                                try {
                                    SettingCategory.valueOf(it.uppercase())
                                } catch (e: IllegalArgumentException) {
                                    // Handle cases where the category name in XML doesn't match an enum constant
                                    null
                                }
                            }
                        }
                        "setting" -> {
                            if (currentCategory != null) {
                                val id = parser.getAttributeValue(null, "id")
                                val nameResId = parser.getAttributeResourceValue(null, "name", 0)
                                val descriptionResId = parser.getAttributeResourceValue(null, "description", 0)
                                val typeName = parser.getAttributeValue(null, "type")

                                if (id != null && typeName != null) {
                                    val type = try {
                                        SettingType.valueOf(typeName.uppercase())
                                    } catch (e: IllegalArgumentException) {
                                        null
                                    }

                                    if (type != null) {
                                        val name = if (nameResId != 0) context.getString(nameResId) else ""
                                        val description = if (descriptionResId != 0) context.getString(descriptionResId) else null

                                        settings.add(
                                            Setting(
                                                id = id,
                                                name = name,
                                                description = description,
                                                type = type,
                                                category = currentCategory
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "category") {
                        currentCategory = null
                    }
                }
            }
            parser.next()
        }

        return settings
    }
}