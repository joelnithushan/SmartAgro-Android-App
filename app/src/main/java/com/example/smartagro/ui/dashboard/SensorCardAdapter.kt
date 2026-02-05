package com.example.smartagro.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagro.R
import com.example.smartagro.databinding.ItemSensorCardBinding
import com.example.smartagro.domain.model.SensorCardData
import com.example.smartagro.domain.model.SensorStatus

class SensorCardAdapter(
    private val sensorCards: List<SensorCardData>
) : RecyclerView.Adapter<SensorCardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSensorCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSensorCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = sensorCards[position]
        val isOffline = card.extra == "Offline"

        holder.binding.tvSensorLabel.text = card.label
        
        // Format value - show "---" when offline
        if (isOffline) {
            holder.binding.tvSensorValue.text = "---"
            holder.binding.tvSensorUnit.text = ""
        } else {
            holder.binding.tvSensorValue.text = formatValue(card)
            holder.binding.tvSensorUnit.text = if (card.unit.isNotBlank()) card.unit else ""
        }
        
        // Set icon with appropriate color
        holder.binding.ivSensorIcon.setImageResource(card.iconResId)
        val iconColorRes = when (card.label) {
            "Air Temp", "Temperature", "Soil Temp" -> R.color.icon_temperature
            "Humidity" -> R.color.icon_humidity
            "Soil Moisture", "Soil Moist Raw" -> R.color.icon_soil
            "AQI", "Air Quality", "CO2", "NH3" -> R.color.icon_air_quality
            else -> R.color.text_color66
        }
        holder.binding.ivSensorIcon.setColorFilter(
            holder.itemView.context.getColor(iconColorRes)
        )

        // Status chip - show status or "Offline"
        val chip = holder.binding.chipStatus
        if (isOffline) {
            chip.text = "Offline"
            chip.setChipBackgroundColorResource(R.color.text_color66)
        } else {
            // For AQI, show the status label from extra (Good/Poor)
            val chipText = when {
                card.label == "AQI" && !card.extra.isNullOrBlank() && card.extra != "Offline" -> card.extra
                card.status == SensorStatus.NORMAL -> "Normal"
                card.status == SensorStatus.LOW -> "Low"
                card.status == SensorStatus.HIGH -> "High"
                else -> "Unknown"
            }
            chip.text = chipText
            
            chip.setChipBackgroundColorResource(
                when (card.status) {
                    SensorStatus.NORMAL -> R.color.status_normal
                    SensorStatus.LOW -> R.color.status_low
                    SensorStatus.HIGH -> R.color.status_high
                }
            )
        }

        // Extra info - show for specific sensors
        val extraView = holder.binding.tvSensorExtra
        when {
            isOffline -> extraView.visibility = View.GONE
            card.label == "AQI" && !card.extra.isNullOrBlank() -> {
                // AQI status already in chip, hide extra
                extraView.visibility = View.GONE
            }
            card.label == "Light" && !card.extra.isNullOrBlank() -> {
                // Show "Detected" or "Dark" as extra
                extraView.visibility = View.VISIBLE
                extraView.text = card.extra
            }
            card.label == "Rain" && !card.extra.isNullOrBlank() -> {
                // Show rain status
                extraView.visibility = View.VISIBLE
                extraView.text = card.extra
            }
            card.label == "Relay" && !card.extra.isNullOrBlank() -> {
                // Show ON/OFF
                extraView.visibility = View.VISIBLE
                extraView.text = card.extra
            }
            else -> extraView.visibility = View.GONE
        }
    }

    private fun formatValue(card: SensorCardData): String {
        return when {
            card.label == "Light" -> {
                // Light shows "Detected" or "Dark" in extra, value is 1 or 0
                if (card.value >= 0.5) "1" else "0"
            }
            card.label == "Relay" -> {
                // Relay shows ON/OFF in extra, value is 1 or 0
                if (card.value >= 0.5) "ON" else "OFF"
            }
            card.unit.isEmpty() -> {
                // No unit - show as integer (AQI, Soil Moist Raw, Rain raw)
                String.format("%.0f", card.value)
            }
            else -> {
                // Show one decimal place for values with units
                String.format("%.1f", card.value)
            }
        }
    }

    override fun getItemCount() = sensorCards.size
}
