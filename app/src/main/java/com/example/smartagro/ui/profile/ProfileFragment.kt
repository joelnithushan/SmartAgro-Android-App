package com.example.smartagro.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.smartagro.LoginActivity
import com.example.smartagro.R
import com.example.smartagro.databinding.ActivityProfileBinding
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {
    
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupLogoutButton()
        setupWhatsAppContact()
    }
    
    private fun setupLogoutButton() {
        val logoutButton: MaterialButton = binding.root.findViewById(R.id.btnLogout)
        logoutButton?.setOnClickListener {
            showLogoutDialog()
        }
    }
    
    private fun setupWhatsAppContact() {
        val whatsappContact = binding.root.findViewById<View>(R.id.whatsapp_contact)
        whatsappContact?.setOnClickListener {
            openWhatsAppChat()
        }
    }
    
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    
    private fun performLogout() {
        val sharedPref = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
    
    private fun openWhatsAppChat() {
        val phoneNumber = "+94769423167"
        val message = "Hi, I need help with the Smart Agro app"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
