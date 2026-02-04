package com.example.smartagro.ui.irrigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartagro.R
import com.example.smartagro.databinding.FragmentIrrigationBinding
import com.example.smartagro.utils.ViewModelFactory
import com.example.smartagro.utils.toTimeAgo
import com.example.smartagro.viewmodel.IrrigationViewModel
import kotlinx.coroutines.launch

class IrrigationFragment : Fragment() {
    
    private var _binding: FragmentIrrigationBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: IrrigationViewModel by viewModels { ViewModelFactory() }
    
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
            viewModel.irrigationStatus.collect { status ->
                status?.let { updateUI(it) }
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.switchAutoMode.isEnabled = !isLoading
            binding.switchManualMode.isEnabled = !isLoading
            binding.btnPump1.isEnabled = !isLoading
            binding.btnPump2.isEnabled = !isLoading
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        
        viewModel.actionSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
                viewModel.clearSuccess()
            }
        }
    }
    
    private fun updateUI(status: com.example.smartagro.domain.model.IrrigationStatus) {
        binding.switchAutoMode.isChecked = status.autoMode
        binding.switchManualMode.isChecked = status.manualMode
        binding.soilMoistureValue.text = "${status.soilMoisture.toInt()}%"
        binding.progressBarMoisture.progress = status.soilMoisture.toInt()
        binding.thresholdValue.text = "${status.threshold.toInt()}%"
        binding.durationValue.text = "${status.duration} minutes"
        
        binding.btnPump1.text = if (status.pump1Status) "Turn OFF" else "Turn ON"
        binding.btnPump2.text = if (status.pump2Status) "Turn OFF" else "Turn ON"
        
        if (status.lastWatered > 0) {
            val lastWateredText = status.lastWatered.toTimeAgo()
        }
    }
    
    private fun setupClickListeners() {
        binding.switchAutoMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.switchManualMode.isChecked = false
                viewModel.setAutoMode(true)
            }
        }
        
        binding.switchManualMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.switchAutoMode.isChecked = false
                viewModel.setManualMode(true)
            }
        }
        
        binding.btnPump1.setOnClickListener {
            viewModel.togglePump(1)
        }
        
        binding.btnPump2.setOnClickListener {
            viewModel.togglePump(2)
        }
    }
    
    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
            findNavController().navigate(R.id.action_irrigation_to_dashboard)
        }
        
        binding.navIrrigation.setOnClickListener {
        }
        
        binding.navChatbot.setOnClickListener {
            findNavController().navigate(R.id.action_irrigation_to_chatbot)
        }
        
        binding.navGuide.setOnClickListener {
            findNavController().navigate(R.id.action_irrigation_to_guide)
        }
        
        binding.navProfile.setOnClickListener {
            findNavController().navigate(R.id.action_irrigation_to_profile)
        }
        
        updateSelectedTab(R.id.nav_irrigation)
    }
    
    private fun updateSelectedTab(selectedId: Int) {
        val tabs = listOf(
            R.id.nav_dashboard, R.id.nav_irrigation, R.id.nav_chatbot,
            R.id.nav_guide, R.id.nav_profile
        )
        
        tabs.forEach { tabId ->
            val tab = binding.root.findViewById<android.widget.LinearLayout>(tabId)
            val text = tab.getChildAt(1) as TextView
            
            if (tabId == selectedId) {
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.button))
            } else {
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
