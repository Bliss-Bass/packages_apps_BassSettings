package com.bass.settings.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsAdapter = SettingsAdapter(viewModel.displayableItems, viewModel)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }

        // Observe the update result event from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateResultEvent.collect { result ->
                    if (!result.success) {
                        // If the setting update failed, show a toast and revert the switch state
                        Toast.makeText(context, "Failed to update setting", Toast.LENGTH_SHORT).show()

                        // Find the index of the failed setting and notify the adapter to re-bind it
                        val index = viewModel.displayableItems.indexOfFirst {
                            it is SettingItem && it.setting.id == result.setting.id
                        }
                        if (index != -1) {
                            settingsAdapter.notifyItemChanged(index)
                        }
                    }
                }
            }
        }
    }
}
