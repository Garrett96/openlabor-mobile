package com.labs.openlabor.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.slider.Slider
import com.labs.openlabor.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupUiElements()
        setupObservers()
        setupListeners()

        return root
    }

    private fun setupUiElements() {
        binding.sliderBreakTime.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            binding.textBreakTimeValue.text = "Default break time: $minutes minutes"
        }
    }

    private fun setupObservers() {
        // Dark mode removed - using single theme

        settingsViewModel.defaultBreakTimeEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.switchBreakDefault.isChecked = enabled
            binding.sliderBreakTime.isEnabled = enabled
        }

        settingsViewModel.defaultBreakTime.observe(viewLifecycleOwner) { breakTime ->
            binding.sliderBreakTime.value = breakTime.toFloat()
            binding.textBreakTimeValue.text = "Default break time: $breakTime minutes"
        }

        settingsViewModel.staffEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.checkboxStaff.isChecked = enabled
        }

        settingsViewModel.tempEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.checkboxTemp.isChecked = enabled
        }

        settingsViewModel.contractorEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.checkboxContractor.isChecked = enabled
        }

        settingsViewModel.managerEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.checkboxManager.isChecked = enabled
        }
    }

    private fun setupListeners() {
        binding.switchBreakDefault.setOnCheckedChangeListener { _, isChecked ->
            binding.sliderBreakTime.isEnabled = isChecked
        }

        binding.buttonSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        settingsViewModel.saveSettings(
            defaultBreakTimeEnabled = binding.switchBreakDefault.isChecked,
            defaultBreakTime = binding.sliderBreakTime.value.toInt(),
            staffEnabled = binding.checkboxStaff.isChecked,
            tempEnabled = binding.checkboxTemp.isChecked,
            contractorEnabled = binding.checkboxContractor.isChecked,
            managerEnabled = binding.checkboxManager.isChecked
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
