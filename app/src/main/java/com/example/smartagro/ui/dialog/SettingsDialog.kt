package com.example.smartagro.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.smartagro.databinding.DialogSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsDialog(
    private val currentFarmId: String,
    private val onFarmIdChanged: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSettingsBinding.inflate(LayoutInflater.from(requireContext()))
        
        binding.etFarmId.setText(currentFarmId)
        binding.etFarmId.selectAll()
        
        binding.btnSave.setOnClickListener {
            val newFarmId = binding.etFarmId.text.toString().trim()
            if (newFarmId.isNotEmpty()) {
                onFarmIdChanged(newFarmId)
                dismiss()
            }
        }
        
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
        
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
