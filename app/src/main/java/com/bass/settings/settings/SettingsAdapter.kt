package com.bass.settings.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bass.settings.R
import com.google.android.material.switchmaterial.SwitchMaterial

private const val VIEW_TYPE_CATEGORY = 0
private const val VIEW_TYPE_SETTING = 1

class SettingsAdapter(
    private val items: List<DisplayableItem>,
    private val viewModel: SettingsViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CategoryItem -> VIEW_TYPE_CATEGORY
            is SettingItem -> VIEW_TYPE_SETTING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_header, parent, false)
                CategoryViewHolder(view)
            }
            VIEW_TYPE_SETTING -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)
                SettingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CategoryItem -> (holder as CategoryViewHolder).bind(item)
            is SettingItem -> (holder as SettingViewHolder).bind(item.setting)
        }
    }

    override fun getItemCount() = items.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: CategoryItem) {
            name.text = category.name
        }
    }

    inner class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.setting_name)
        private val description: TextView = itemView.findViewById(R.id.setting_description)
        private val switch: SwitchMaterial = itemView.findViewById(R.id.setting_switch)

        fun bind(setting: Setting) {
            name.text = setting.name

            if (!setting.description.isNullOrEmpty()) {
                description.text = setting.description
                description.visibility = View.VISIBLE
            } else {
                description.visibility = View.GONE
            }

            if (setting.value is Boolean) {
                switch.isChecked = setting.value as Boolean
            } else if (setting.value is Int) {
                switch.isChecked = (setting.value as Int) == 1
            }

            // Set listener to null before setting checked state to prevent unwanted calls
            switch.setOnCheckedChangeListener(null)
            if (setting.value is Boolean) {
                switch.isChecked = setting.value as Boolean
            } else if (setting.value is Int) {
                switch.isChecked = (setting.value as Int) == 1
            }

            switch.setOnCheckedChangeListener { _, isChecked ->
                val newValue = when (setting.defaultValue) {
                    is Boolean -> isChecked
                    is Int -> if (isChecked) 1 else 0
                    else -> setting.value // Should not happen for switch items
                }
                viewModel.updateSetting(setting, newValue)
            }
        }
    }
}
