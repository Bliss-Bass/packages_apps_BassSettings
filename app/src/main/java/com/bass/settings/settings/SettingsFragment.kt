/*
 * Copyright (C) 2025 The Bass Project
 *
 * This file is part of The Bass Project.
 *
 * The Bass Project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Bass Project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The Bass Project. If not, see <https://www.gnu.org/licenses/>.
 *
 * For commercial licensing options, please contact info@navotpala.tech
 */
package com.bass.settings.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bass.settings.R
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsAdapter = SettingsAdapter {
            settingItem, isChecked -> viewModel.updateSetting(settingItem.setting.id, isChecked)
        }
        
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
            // To prevent the "blinking" issue when an item updates
            itemAnimator = null 
        }

        // Observe the UI state from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is SettingsUiState.Loading -> {
                            // Optionally, show a loading spinner for the whole screen
                        }
                        is SettingsUiState.Success -> {
                            settingsAdapter.submitList(uiState.items)
                        }
                    }
                }
            }
        }
    }
}
