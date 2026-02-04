package com.example.smartagro.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartagro.R
import com.example.smartagro.databinding.FragmentDashboardBinding
import com.example.smartagro.utils.ViewModelFactory
import com.example.smartagro.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels { ViewModelFactory() }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupBottomNavigation()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sensorData.collect { sensorData ->
                sensorData?.let { updateUI(it) }
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUI(data: com.example.smartagro.domain.model.SensorData) {
    }
    
    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
        }
        
        binding.navIrrigation.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_irrigation)
        }
        
        binding.navChatbot.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_chatbot)
        }
        
        binding.navGuide.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_guide)
        }
        
        binding.navProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profile)
        }
        
        updateSelectedTab(R.id.nav_dashboard)
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
