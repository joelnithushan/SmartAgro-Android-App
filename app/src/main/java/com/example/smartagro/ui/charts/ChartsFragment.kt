package com.example.smartagro.ui.charts

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartagro.R
import com.example.smartagro.databinding.FragmentChartsBinding
import com.example.smartagro.viewmodel.MonitoringViewModel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!

    private val monitoringViewModel: MonitoringViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCharts()
        setupObservers()
        setupBottomNavigation()
    }

    private fun setupCharts() {
        setupTempHumidityChart()
        setupSoilAqiChart()
    }

    private fun setupTempHumidityChart() {
        val chart = binding.chartTempHumidity

        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.legend.isEnabled = true

        // X Axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        xAxis.setDrawGridLines(false)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.granularity = 1f

        // Left Y Axis
        val leftAxis = chart.axisLeft
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.text_tertiary)
        leftAxis.axisMinimum = 0f

        // Right Y Axis
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        // Legend
        val legend = chart.legend
        legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)

        chart.setNoDataText("No data available")
        chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary))
    }

    private fun setupSoilAqiChart() {
        val chart = binding.chartSoilAqi

        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)
        chart.legend.isEnabled = true

        // X Axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        xAxis.setDrawGridLines(false)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.granularity = 1f

        // Left Y Axis
        val leftAxis = chart.axisLeft
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.text_tertiary)
        leftAxis.axisMinimum = 0f

        // Right Y Axis
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        // Legend
        val legend = chart.legend
        legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)

        chart.setNoDataText("No data available")
        chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary))
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            monitoringViewModel.chartData.collect { chartData ->
                updateTempHumidityChart(chartData.tempHumidityData)
                updateSoilAqiChart(chartData.soilAqiData)
            }
        }
    }

    private fun updateTempHumidityChart(data: List<Pair<Float, Pair<Float, Float>>>) {
        val chart = binding.chartTempHumidity

        if (data.isEmpty()) {
            chart.clear()
            return
        }

        val tempEntries = mutableListOf<Entry>()
        val humidityEntries = mutableListOf<Entry>()

        data.forEachIndexed { index, (_, values) ->
            tempEntries.add(Entry(index.toFloat(), values.first))
            humidityEntries.add(Entry(index.toFloat(), values.second))
        }

        val tempDataSet = LineDataSet(tempEntries, "Temperature (°C)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.accent_temperature)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent_temperature))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            valueTextSize = 10f
            axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT
        }

        val humidityDataSet = LineDataSet(humidityEntries, "Humidity (%)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.accent_humidity)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent_humidity))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            valueTextSize = 10f
            axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT
        }

        val lineData = LineData(tempDataSet, humidityDataSet)
        chart.data = lineData
        chart.invalidate()
    }

    private fun updateSoilAqiChart(data: List<Pair<Float, Pair<Float, Float>>>) {
        val chart = binding.chartSoilAqi

        if (data.isEmpty()) {
            chart.clear()
            return
        }

        val soilEntries = mutableListOf<Entry>()
        val aqiEntries = mutableListOf<Entry>()

        data.forEachIndexed { index, (_, values) ->
            soilEntries.add(Entry(index.toFloat(), values.first))
            aqiEntries.add(Entry(index.toFloat(), values.second))
        }

        val soilDataSet = LineDataSet(soilEntries, "Soil Moisture (%)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.accent_soil)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent_soil))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            valueTextSize = 10f
            axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT
        }

        val aqiDataSet = LineDataSet(aqiEntries, "Air Quality Index").apply {
            color = ContextCompat.getColor(requireContext(), R.color.accent_air_quality)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent_air_quality))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            valueTextSize = 10f
            axisDependency = com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT
        }

        val lineData = LineData(soilDataSet, aqiDataSet)
        chart.data = lineData
        chart.invalidate()
    }

    private fun setupBottomNavigation() {
        binding.navDashboard.setOnClickListener {
            findNavController().navigate(R.id.action_charts_to_dashboard)
            updateSelectedTab(R.id.nav_dashboard)
        }

        binding.navCharts.setOnClickListener {
            // Already on charts
            updateSelectedTab(R.id.nav_charts)
        }

        binding.navIrrigation.setOnClickListener {
            findNavController().navigate(R.id.action_charts_to_irrigation)
            updateSelectedTab(R.id.nav_irrigation)
        }
        
        updateSelectedTab(R.id.nav_charts)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
