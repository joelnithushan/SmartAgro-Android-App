package com.example.smartagro.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartagro.R
import com.example.smartagro.databinding.ItemSensorCardDarkBinding
import com.example.smartagro.domain.model.SensorCardData
import com.example.smartagro.domain.model.SensorStatus
import com.example.smartagro.utils.SensorColorMapper

class SensorCardDarkAdapter : ListAdapter<SensorCardData, SensorCardDarkAdapter.ViewHolder>(SensorCardDiffCallback()) {

    class ViewHolder(val binding: ItemSensorCardDarkBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSensorCardDarkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = getItem(position)
        val isOffline = card.extra == "Offline"
        val context = holder.itemView.context

        // Set label
        holder.binding.tvSensorLabel.text = card.label

        // Apply parameter-specific colors first
        val accentColorRes = SensorColorMapper.getAccentColorResId(card.label)
        val accentColor = ContextCompat.getColor(context, accentColorRes)
        
        // Set left vertical accent bar color
        holder.binding.viewAccentBar.setBackgroundColor(accentColor)
        
        // Set icon chip background color (semi-transparent accent color)
        val iconChipBgColor = Color.parseColor(SensorColorMapper.getIconChipBackgroundColor(card.label))
        holder.binding.cardIconChip.setCardBackgroundColor(iconChipBgColor)
        
        // Set icon (PNG icons - no color filter for realistic icons)
        holder.binding.ivSensorIcon.setImageResource(card.iconResId)
        holder.binding.ivSensorIcon.clearColorFilter()

        // Format value - show "---" when offline
        if (isOffline) {
            holder.binding.tvSensorValue.text = "---"
            holder.binding.tvSensorUnit.text = ""
            holder.binding.tvSensorValue.setTextColor(ContextCompat.getColor(context, R.color.text_tertiary))
        } else {
            holder.binding.tvSensorValue.text = formatValue(card)
            holder.binding.tvSensorUnit.text = if (card.unit.isNotBlank()) card.unit else ""
            // Set value text color to accent color for emphasis
            holder.binding.tvSensorValue.setTextColor(accentColor)
        }

        // Status pill chip with colored background
        val chip = holder.binding.chipStatus
        val statusText = when {
            isOffline -> "Offline"
            card.label == "AQI" && !card.extra.isNullOrBlank() && card.extra != "Offline" -> card.extra
            card.label == "Relay" && !card.extra.isNullOrBlank() -> card.extra
            card.status == SensorStatus.NORMAL -> "Normal"
            card.status == SensorStatus.LOW -> "Low"
            card.status == SensorStatus.HIGH -> "High"
            else -> "Normal"
        }
        chip.text = statusText
        
        // Set chip background color based on status
        chip.setChipBackgroundColorResource(
            when {
                isOffline -> R.color.text_tertiary
                card.label == "AQI" && card.status == SensorStatus.HIGH -> R.color.status_high
                card.status == SensorStatus.NORMAL -> R.color.status_normal
                card.status == SensorStatus.LOW -> R.color.status_low
                card.status == SensorStatus.HIGH -> R.color.status_high
                else -> R.color.status_normal
            }
        )
        chip.visibility = View.VISIBLE
    }

    private fun formatValue(card: SensorCardData): String {
        return when {
            card.label == "Light" -> {
                if (card.value >= 0.5) "1" else "0"
            }
            card.label == "Relay" -> {
                if (card.value >= 0.5) "ON" else "OFF"
            }
            card.unit.isEmpty() -> {
                String.format("%.0f", card.value)
            }
            else -> {
                String.format("%.1f", card.value)
            }
        }
    }
}

/**
 * DiffUtil callback to efficiently update RecyclerView items without recreating the entire list.
 * This preserves scroll position when data updates.
 */
class SensorCardDiffCallback : DiffUtil.ItemCallback<SensorCardData>() {
    override fun areItemsTheSame(oldItem: SensorCardData, newItem: SensorCardData): Boolean {
        // Use label as stable ID since each sensor type has a unique label
        return oldItem.label == newItem.label
    }

    override fun areContentsTheSame(oldItem: SensorCardData, newItem: SensorCardData): Boolean {
        // Check if the actual content changed
        return oldItem.value == newItem.value &&
                oldItem.status == newItem.status &&
                oldItem.unit == newItem.unit &&
                oldItem.extra == newItem.extra &&
                oldItem.iconResId == newItem.iconResId
    }
}
