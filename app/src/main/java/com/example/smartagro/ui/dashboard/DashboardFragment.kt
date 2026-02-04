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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartagro.R
import com.example.smartagro.databinding.FragmentDashboardBinding
import com.example.smartagro.domain.model.firestore.UserDevice
import com.example.smartagro.utils.DeviceViewModelFactory
import com.example.smartagro.utils.Constants
import com.example.smartagro.utils.GridSpacingItemDecoration
import com.example.smartagro.utils.formatTimestamp
import com.example.smartagro.viewmodel.DeviceSelectionViewModel
import com.example.smartagro.viewmodel.MonitoringViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val deviceFactory = DeviceViewModelFactory()
    private val deviceSelectionViewModel: DeviceSelectionViewModel by viewModels { deviceFactory }
    private val monitoringViewModel: MonitoringViewModel by viewModels { deviceFactory }
    private lateinit var adapter: SensorCardAdapter
    
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

        deviceSelectionViewModel.start()
        monitoringViewModel.observe(deviceSelectionViewModel.selectedDeviceId)
    }
    
    private fun setupRecyclerView() {
        adapter = SensorCardAdapter(emptyList())
        binding.rvSensorCards.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSensorCards.adapter = adapter
        val spacing = resources.getDimensionPixelSize(R.dimen.space_md)
        if (binding.rvSensorCards.itemDecorationCount == 0) {
            binding.rvSensorCards.addItemDecoration(GridSpacingItemDecoration(2, spacing))
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.button)
        )
        binding.swipeRefresh.setOnRefreshListener {
            val selected = deviceSelectionViewModel.selectedDeviceId.value
            if (selected != null) {
                monitoringViewModel.observe(deviceSelectionViewModel.selectedDeviceId)
            }
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            monitoringViewModel.sensorCards.collect { cards ->
                if (cards.isNotEmpty()) {
                    adapter = SensorCardAdapter(cards)
                    binding.rvSensorCards.adapter = adapter
                    animateViewVisibility(binding.rvSensorCards, View.VISIBLE)
                    animateViewVisibility(binding.emptyState, View.GONE)
                    animateViewVisibility(binding.errorState, View.GONE)
                } else {
                    animateViewVisibility(binding.rvSensorCards, View.GONE)
                    if (monitoringViewModel.errorMessage.value == null) {
                        animateViewVisibility(binding.emptyState, View.VISIBLE)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            deviceSelectionViewModel.selectedDeviceId.collect { deviceId ->
                binding.tvFarmName.text = deviceId ?: "-"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            deviceSelectionViewModel.devices.collect { devices ->
                setupDevicePicker(devices)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            monitoringViewModel.lastSeen.collect { ts ->
                binding.tvLastSeen.text = "Last seen: ${formatTimestamp(ts)}"
            }
        }
        
        monitoringViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            
            if (isLoading && adapter.itemCount == 0) {
                animateViewVisibility(binding.loadingState, View.VISIBLE)
                animateViewVisibility(binding.rvSensorCards, View.GONE)
                animateViewVisibility(binding.emptyState, View.GONE)
                animateViewVisibility(binding.errorState, View.GONE)
            } else {
                animateViewVisibility(binding.loadingState, View.GONE)
            }
        }
        
        monitoringViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                animateViewVisibility(binding.errorState, View.VISIBLE)
                animateViewVisibility(binding.rvSensorCards, View.GONE)
                animateViewVisibility(binding.loadingState, View.GONE)
                animateViewVisibility(binding.emptyState, View.GONE)
                binding.tvErrorMessage.text = error
            } else {
                animateViewVisibility(binding.errorState, View.GONE)
            }
        }
    }

    private fun setupDevicePicker(devices: List<UserDevice>) {
        val items = devices.map { deviceLabel(it) }
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        binding.actDevicePicker.setAdapter(adapter)

        val selectedId = deviceSelectionViewModel.selectedDeviceId.value
        val selectedIndex = devices.indexOfFirst { it.deviceId == selectedId }
        if (selectedIndex >= 0) {
            binding.actDevicePicker.setText(items[selectedIndex], false)
        } else if (items.isNotEmpty() && binding.actDevicePicker.text.isNullOrBlank()) {
            binding.actDevicePicker.setText(items.first(), false)
        }

        binding.actDevicePicker.setOnItemClickListener { _, _, position, _ ->
            val selected = devices.getOrNull(position)?.deviceId ?: return@setOnItemClickListener
            deviceSelectionViewModel.selectDevice(selected)
        }
    }

    private fun deviceLabel(device: UserDevice): String {
        val name = device.farmName?.takeIf { it.isNotBlank() } ?: device.deviceId
        val loc = device.location?.takeIf { it.isNotBlank() }
        return if (loc != null) "$name • $loc" else name
    }
    
    private fun setupClickListeners() {
        binding.btnGoToIrrigation.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_irrigation)
        }
        
        binding.btnRetry.setOnClickListener {
            binding.errorState.visibility = View.GONE
            monitoringViewModel.errorMessage.value?.let { }
            val selected = deviceSelectionViewModel.selectedDeviceId.value
            if (selected != null) {
                monitoringViewModel.observe(deviceSelectionViewModel.selectedDeviceId)
            }
        }
        
        binding.ivSettings.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun showSettingsDialog() {
        val currentFarmId = Constants.DEFAULT_FARM_ID
        val dialog = com.example.smartagro.ui.dialog.SettingsDialog(currentFarmId) { newFarmId ->
            if (newFarmId != currentFarmId) {
                Constants.setFarmId(newFarmId)
            }
        }
        dialog.show(parentFragmentManager, "SettingsDialog")
    }
    
    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
        }
        
        binding.navIrrigation.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_irrigation)
        }
        
        updateSelectedTab(R.id.nav_dashboard)
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
