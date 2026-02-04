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
import com.example.smartagro.domain.model.IrrigationState
import com.example.smartagro.utils.Constants
import com.example.smartagro.utils.ViewModelFactory
import com.example.smartagro.utils.toTimeAgo
import com.example.smartagro.viewmodel.IrrigationViewModel
import kotlinx.coroutines.launch

class IrrigationFragment : Fragment() {
    
    private var _binding: FragmentIrrigationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: IrrigationViewModel by viewModels { 
        ViewModelFactory(Constants.DEFAULT_FARM_ID) 
    }
    
    private var thresholdUpdateHandler: android.os.Handler? = null
    private val thresholdUpdateRunnable = object : Runnable {
        override fun run() {
            val currentValue = binding.sliderThreshold.value.toDouble()
            viewModel.setThreshold(currentValue)
        }
    }
    
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
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.irrigationState.collect { state ->
                state?.let { updateUI(it) }
            }
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            animateViewVisibility(binding.loadingState, if (isLoading) View.VISIBLE else View.GONE)
            
            binding.switchManualToggle.isEnabled = !isLoading
            binding.toggleMode.isEnabled = !isLoading
            
            val isAutoMode = viewModel.irrigationState.value?.mode == "AUTO"
            binding.sliderThreshold.isEnabled = !isLoading && isAutoMode
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                animateViewVisibility(binding.errorState, View.VISIBLE)
                binding.tvErrorMessage.text = error
            } else {
                animateViewVisibility(binding.errorState, View.GONE)
            }
        }
        
        viewModel.isAutoModeActive.observe(viewLifecycleOwner) { isActive ->
            animateViewVisibility(binding.autoModeBanner, if (isActive) View.VISIBLE else View.GONE)
        }
    }
    
    private fun updateUI(state: IrrigationState) {
        binding.tvIrrigationStatus.text = if (state.isOn) "ON" else "OFF"
        binding.tvIrrigationStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (state.isOn) R.color.status_normal else R.color.status_high
            )
        )
        
        binding.switchManualToggle.setOnCheckedChangeListener(null)
        binding.switchManualToggle.isChecked = state.isOn
        setupToggleListener()
        
        when (state.mode) {
            "MANUAL" -> {
                binding.toggleMode.check(R.id.btn_mode_manual)
                binding.autoModeSettings.visibility = View.GONE
            }
            "AUTO" -> {
                binding.toggleMode.check(R.id.btn_mode_auto)
                binding.autoModeSettings.visibility = View.VISIBLE
                binding.sliderThreshold.value = state.moistureThreshold.toFloat()
                binding.tvThresholdValue.text = "${state.moistureThreshold.toInt()}%"
            }
        }
        
        if (state.lastChangedAt > 0) {
            binding.tvLastActionTime.text = state.lastChangedAt.toTimeAgo()
            binding.tvLastActionSource.text = "Source: ${state.lastChangedBy}"
        } else {
            binding.tvLastActionTime.text = "Never"
            binding.tvLastActionSource.text = "Source: -"
        }
    }
    
    private fun setupToggleListener() {
        binding.switchManualToggle.setOnCheckedChangeListener { _, isChecked ->
            val currentState = viewModel.irrigationState.value
            if (isChecked != currentState?.isOn) {
                showToggleConfirmation(isChecked)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_mode_manual -> {
                        viewModel.setMode("MANUAL")
                    }
                    R.id.btn_mode_auto -> {
                        viewModel.setMode("AUTO")
                    }
                }
            }
        }
        
        binding.sliderThreshold.addOnChangeListener { _, value, fromUser ->
            binding.tvThresholdValue.text = "${value.toInt()}%"
            
            if (fromUser) {
                thresholdUpdateHandler?.removeCallbacks(thresholdUpdateRunnable)
                thresholdUpdateHandler = android.os.Handler(android.os.Looper.getMainLooper())
                thresholdUpdateHandler?.postDelayed(thresholdUpdateRunnable, 500)
            }
        }
        
        binding.btnRetry.setOnClickListener {
            binding.errorState.visibility = View.GONE
            viewModel.clearError()
            viewModel.refreshStatus()
        }
    }
    
    private fun showToggleConfirmation(turnOn: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle("Turn irrigation ${if (turnOn) "ON" else "OFF"}?")
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.toggleIrrigation(true)
            }
            .setNegativeButton("Cancel") { _, _ ->
                val currentState = viewModel.irrigationState.value
                binding.switchManualToggle.setOnCheckedChangeListener(null)
                binding.switchManualToggle.isChecked = currentState?.isOn ?: false
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
        thresholdUpdateHandler?.removeCallbacks(thresholdUpdateRunnable)
        thresholdUpdateHandler = null
        _binding = null
    }
}
