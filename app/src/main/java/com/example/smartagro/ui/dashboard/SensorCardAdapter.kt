package com.example.smartagro.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagro.R
import com.example.smartagro.databinding.ItemSensorCardBinding
import com.example.smartagro.domain.model.SensorCardData
import com.example.smartagro.domain.model.SensorStatus
import com.google.android.material.chip.Chip

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
        
        holder.binding.tvSensorLabel.text = card.label
        holder.binding.tvSensorValue.text = formatValue(card.value, card.unit)
        holder.binding.tvSensorUnit.text = card.unit
        holder.binding.ivSensorIcon.setImageResource(card.iconResId)
        
        val chip = holder.binding.chipStatus
        chip.text = when (card.status) {
            SensorStatus.NORMAL -> "Normal"
            SensorStatus.LOW -> "Low"
            SensorStatus.HIGH -> "High"
        }
        
        chip.setChipBackgroundColorResource(
            when (card.status) {
                SensorStatus.NORMAL -> R.color.status_normal
                SensorStatus.LOW -> R.color.status_low
                SensorStatus.HIGH -> R.color.status_high
            }
        )
    }
    
    private fun formatValue(value: Double, unit: String): String {
        return String.format("%.1f", value)
    }

    override fun getItemCount() = sensorCards.size
}
