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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartagro.R
import com.example.smartagro.databinding.FragmentDashboardBinding
import com.example.smartagro.utils.ViewModelFactory
import com.example.smartagro.utils.Constants
import com.example.smartagro.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels { 
        ViewModelFactory(Constants.DEFAULT_FARM_ID) 
    }
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
    }
    
    private fun setupRecyclerView() {
        adapter = SensorCardAdapter(emptyList())
        binding.rvSensorCards.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSensorCards.adapter = adapter
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.button)
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sensorCards.collect { cards ->
                if (cards.isNotEmpty()) {
                    adapter = SensorCardAdapter(cards)
                    binding.rvSensorCards.adapter = adapter
                    animateViewVisibility(binding.rvSensorCards, View.VISIBLE)
                    animateViewVisibility(binding.emptyState, View.GONE)
                    animateViewVisibility(binding.errorState, View.GONE)
                } else {
                    animateViewVisibility(binding.rvSensorCards, View.GONE)
                    if (viewModel.errorMessage.value == null) {
                        animateViewVisibility(binding.emptyState, View.VISIBLE)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sensorSnapshot.collect { snapshot ->
                snapshot?.let {
                    binding.tvFarmName.text = Constants.DEFAULT_FARM_ID
                }
            }
        }
        
        viewModel.farmName.observe(viewLifecycleOwner) { name ->
            binding.tvFarmName.text = name
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
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
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
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
    
    private fun setupClickListeners() {
        binding.btnGoToIrrigation.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_irrigation)
        }
        
        binding.btnRetry.setOnClickListener {
            binding.errorState.visibility = View.GONE
            viewModel.clearError()
            viewModel.refreshData()
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
                viewModel.updateFarmId(newFarmId)
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
