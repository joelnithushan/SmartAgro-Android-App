package com.example.smartagro.ui.dashboard

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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartagro.R
import com.example.smartagro.databinding.FragmentDashboardBinding
import com.example.smartagro.utils.DeviceConfig
import com.example.smartagro.utils.GridSpacingItemDecoration
import com.example.smartagro.utils.formatTimestamp
import com.example.smartagro.viewmodel.MonitoringViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val monitoringViewModel: MonitoringViewModel by viewModels()
    private lateinit var adapter: SensorCardDarkAdapter
    
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
        
        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()
        setupClickListeners()
        setupBottomNavigation()
    }
    
    private fun setupRecyclerView() {
        adapter = SensorCardDarkAdapter()
        binding.rvSensorCards.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvSensorCards.adapter = adapter
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.button)
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            monitoringViewModel.sensorCards.collect { cards ->
                // Use submitList instead of recreating adapter - this preserves scroll position
                adapter.submitList(cards)
                binding.rvSensorCards.visibility = View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            monitoringViewModel.lastReceivedTime.collect { receivedTime ->
                binding.tvLastUpdated.text = formatLastUpdated(receivedTime)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            monitoringViewModel.deviceUptime.collect { uptime ->
                // Uptime is shown separately if needed
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            monitoringViewModel.online.collect { online ->
                binding.tvOnlineStatus.text = if (online) "Online" else "Offline"
            }
        }
        
        monitoringViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
        
        monitoringViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                android.widget.Toast.makeText(requireContext(), error, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivSettings.setOnClickListener {
            showDeviceIdDialog()
        }
    }

    private fun showDeviceIdDialog() {
        val context = requireContext()
        val input = android.widget.EditText(context)
        input.setText(DeviceConfig.currentDeviceId)
        input.setSelection(input.text.length)

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Set Device ID")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newId = input.text.toString().trim()
                DeviceConfig.updateDeviceId(context.applicationContext, newId)
                binding.tvFarmName.text = DeviceConfig.currentDeviceId
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
            // Already on dashboard
            updateSelectedTab(R.id.nav_dashboard)
        }
        
        binding.navCharts.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_charts)
            updateSelectedTab(R.id.nav_charts)
        }
        
        binding.navIrrigation.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_irrigation)
            updateSelectedTab(R.id.nav_irrigation)
        }
        
        updateSelectedTab(R.id.nav_dashboard)
    }
    
    private fun updateSelectedTab(selectedId: Int) {
        val tabs = listOf(R.id.nav_dashboard, R.id.nav_charts, R.id.nav_irrigation)
        
        tabs.forEach { tabId ->
            val tab = binding.root.findViewById<android.widget.LinearLayout>(tabId)
            val icon = tab.getChildAt(0) as android.widget.ImageView
            val text = tab.getChildAt(1) as TextView
            
            if (tabId == selectedId) {
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_primary))
                icon.alpha = 1.0f
            } else {
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary))
                icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.text_tertiary))
                icon.alpha = 1.0f
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
    
    private fun formatLastUpdated(receivedTime: Long?): String {
        if (receivedTime == null || receivedTime <= 0) {
            return "Last updated: Never"
        }
        val now = System.currentTimeMillis()
        val diff = now - receivedTime
        val seconds = diff / 1000
        val minutes = seconds / 60
        
        return when {
            seconds < 10 -> "Last updated: Just now"
            seconds < 60 -> "Last updated: ${seconds}s ago"
            minutes < 60 -> "Last updated: ${minutes}m ago"
            else -> "Last updated: ${minutes / 60}h ago"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
