package com.example.smartagro.ui.irrigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartagro.R
import com.example.smartagro.databinding.FragmentIrrigationBinding
import com.example.smartagro.utils.DeviceViewModelFactory
import com.example.smartagro.utils.formatTimestamp
import com.example.smartagro.viewmodel.DeviceSelectionViewModel
import com.example.smartagro.viewmodel.IrrigationRtdbViewModel
import com.example.smartagro.viewmodel.IrrigationUiState
import kotlinx.coroutines.launch

class IrrigationFragment : Fragment() {
    
    private var _binding: FragmentIrrigationBinding? = null
    private val binding get() = _binding!!
    
    private val deviceFactory = DeviceViewModelFactory()
    private val deviceSelectionViewModel: DeviceSelectionViewModel by viewModels { deviceFactory }
    private val viewModel: IrrigationRtdbViewModel by viewModels { deviceFactory }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIrrigationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
        setupBottomNavigation()

        deviceSelectionViewModel.start()
        viewModel.observe(deviceSelectionViewModel.selectedDeviceId)
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUiFromRtdb(state)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            animateViewVisibility(binding.loadingState, if (isLoading) View.VISIBLE else View.GONE)
            binding.switchManualToggle.isEnabled = !isLoading && (viewModel.writing.value != true)
        }

        viewModel.writing.observe(viewLifecycleOwner) { isWriting ->
            binding.switchManualToggle.isEnabled = !isWriting && (viewModel.loading.value != true)
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                animateViewVisibility(binding.errorState, View.VISIBLE)
                binding.tvErrorMessage.text = error
            } else {
                animateViewVisibility(binding.errorState, View.GONE)
            }
        }
    }
    
    private fun updateUiFromRtdb(state: IrrigationUiState) {
        val isOn = state.isOn
        binding.tvIrrigationStatus.text = when (isOn) {
            true -> "ON"
            false -> "OFF"
            null -> "--"
        }
        binding.tvIrrigationStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isOn == true) R.color.status_normal else R.color.status_high
            )
        )
        
        binding.switchManualToggle.setOnCheckedChangeListener(null)
        binding.switchManualToggle.isChecked = isOn == true
        setupToggleListener()

        val relay = state.relayControl
        val ts = relay?.timestamp ?: state.relayStatusUi?.timestamp
        if (ts != null && ts > 0) {
            binding.tvLastActionTime.text = formatTimestamp(ts)
        } else {
            binding.tvLastActionTime.text = formatTimestamp(null)
        }
        val by = relay?.lastChangedBy ?: state.relayStatusUi?.requestedBy ?: "—"
        binding.tvLastActionSource.text = "Source: $by"
    }
    
    private fun setupToggleListener() {
        binding.switchManualToggle.setOnCheckedChangeListener { _, isChecked ->
            val currentIsOn = viewModel.uiState.value.isOn
            if (currentIsOn == null || isChecked != currentIsOn) {
                showToggleConfirmation(isChecked)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            binding.errorState.visibility = View.GONE
        }
    }
    
    private fun showToggleConfirmation(turnOn: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle("Turn irrigation ${if (turnOn) "ON" else "OFF"}?")
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.writeRelay(turnOn, mirrorStatus = true)
            }
            .setNegativeButton("Cancel") { _, _ ->
                val currentIsOn = viewModel.uiState.value.isOn
                binding.switchManualToggle.setOnCheckedChangeListener(null)
                binding.switchManualToggle.isChecked = currentIsOn == true
                setupToggleListener()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
            findNavController().navigate(R.id.action_irrigation_to_dashboard)
        }
        
        binding.navIrrigation.setOnClickListener {
        }
        
        updateSelectedTab(R.id.nav_irrigation)
    }
    
    private fun updateSelectedTab(selectedId: Int) {
        val tabs = listOf(R.id.nav_dashboard, R.id.nav_irrigation)
        
        tabs.forEach { tabId ->
            val tab = binding.root.findViewById<android.widget.LinearLayout>(tabId)
            val text = tab.getChildAt(1) as TextView
            
            if (tabId == selectedId) {
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.button))
                tab.getChildAt(0).alpha = 1.0f
            } else {
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tab.getChildAt(0).alpha = 0.6f
            }
        }
    }
    
    private fun animateViewVisibility(view: View, visibility: Int) {
        if (visibility == View.VISIBLE) {
            if (view.visibility == View.VISIBLE) return
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                .alpha(1f)
                .setDuration(250)
                .setListener(null)
            return
        }

        if (view.visibility != View.VISIBLE) return
        view.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                view.visibility = visibility
            }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
