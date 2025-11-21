package com.bass.settings.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bass.settings.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsAdapter(
    private val settings: List<Setting>,
    private val viewModel: SettingsViewModel
) : RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)
        return SettingViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        holder.bind(settings[position])
    }

    override fun getItemCount() = settings.size

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

            switch.setOnCheckedChangeListener { _, isChecked ->
                val newValue = when (setting.defaultValue) {
                    is Boolean -> isChecked
                    is Int -> if (isChecked) 1 else 0
                    else -> setting.value
                }
                viewModel.updateSetting(setting, newValue)
            }
        }
    }
}
