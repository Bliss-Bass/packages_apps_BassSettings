package com.bass.settings.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bass.settings.R
import com.google.android.material.switchmaterial.SwitchMaterial

private const val VIEW_TYPE_CATEGORY = 0
private const val VIEW_TYPE_SETTING = 1

class SettingsAdapter(
    private val onSettingClicked: (SettingItem, Boolean) -> Unit
) : ListAdapter<DisplayableItem, RecyclerView.ViewHolder>(SettingDiffCallback()) {

    override fun getItemViewType(position: Int):
        return when (getItem(position)) {
            is CategoryItem -> VIEW_TYPE_CATEGORY
            is SettingItem -> VIEW_TYPE_SETTING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                val view = inflater.inflate(R.layout.item_category, parent, false)
                CategoryViewHolder(view)
            }
            VIEW_TYPE_SETTING -> {
                val view = inflater.inflate(R.layout.item_setting, parent, false)
                SettingViewHolder(view, onSettingClicked)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryItem -> (holder as CategoryViewHolder).bind(item)
            is SettingItem -> (holder as SettingViewHolder).bind(item)
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.category_name)
        fun bind(category: CategoryItem) {
            nameTextView.text = category.name
        }
    }

    class SettingViewHolder(
        itemView: View,
        private val onSettingClicked: (SettingItem, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.setting_name)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.setting_description)
        private val switchWidget: SwitchMaterial = itemView.findViewById(R.id.setting_switch)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.setting_progress_bar)

        fun bind(item: SettingItem) {
            nameTextView.text = item.setting.name
            descriptionTextView.text = item.setting.description

            // Show/hide the progress bar and switch based on the updating state
            if (item.isUpdating) {
                switchWidget.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            } else {
                switchWidget.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
            
            // Set the switch state without triggering the listener
            switchWidget.setOnCheckedChangeListener(null)
            switchWidget.isChecked = item.value
            switchWidget.setOnCheckedChangeListener { _, isChecked ->
                onSettingClicked(item, isChecked)
            }

            // Also handle clicks on the main item view
            itemView.setOnClickListener {
                switchWidget.toggle()
            }
        }
    }
}

class SettingDiffCallback : DiffUtil.ItemCallback<DisplayableItem>() {
    override fun areItemsTheSame(oldItem: DisplayableItem, newItem: DisplayableItem): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: DisplayableItem, newItem: DisplayableItem): Boolean {
        // The `SettingItem` data class will handle content comparison
        return oldItem == newItem
    }
}
