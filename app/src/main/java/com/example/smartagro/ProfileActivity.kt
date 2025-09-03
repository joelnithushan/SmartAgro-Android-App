package com.example.smartagro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        setupLogoutButton()
        setupWhatsAppContact()
        updateSelectedTab(R.id.nav_profile)
    }

    private fun setupBottomNavigation() {
        val navDashboard: LinearLayout = findViewById(R.id.nav_dashboard)
        navDashboard.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            updateSelectedTab(R.id.nav_dashboard)
        }

        val navIrrigation: LinearLayout = findViewById(R.id.nav_irrigation)
        navIrrigation.setOnClickListener {
            val intent = Intent(this, IrrigationActivity::class.java)
            startActivity(intent)
            updateSelectedTab(R.id.nav_irrigation)
        }

        val navChatbot: LinearLayout = findViewById(R.id.nav_chatbot)
        navChatbot.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java)
            startActivity(intent)
            updateSelectedTab(R.id.nav_chatbot)
        }

        val navGuide: LinearLayout = findViewById(R.id.nav_guide)
        navGuide.setOnClickListener {
            val intent = Intent(this, GuideActivity::class.java)
            startActivity(intent)
            updateSelectedTab(R.id.nav_guide)
        }

        val navProfile: LinearLayout = findViewById(R.id.nav_profile)
        navProfile.setOnClickListener {
            updateSelectedTab(R.id.nav_profile)
        }
    }

    private fun setupLogoutButton() {
        val logoutButton: MaterialButton = findViewById(R.id.btnLogout)
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupWhatsAppContact() {
        val whatsappContact: LinearLayout = findViewById(R.id.whatsapp_contact)
        whatsappContact.setOnClickListener {
            openWhatsAppChat()
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
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
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun openWhatsAppChat() {
        val phoneNumber = "+94769423167"
        val message = "Hi, I need help with the Smart Agro app"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
            startActivity(intent)
        } catch (e: Exception) {

            Toast.makeText(this, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSelectedTab(selectedId: Int) {
        val tabs = listOf(
            R.id.nav_dashboard, R.id.nav_irrigation, R.id.nav_chatbot,
            R.id.nav_guide, R.id.nav_profile
        )

        tabs.forEach { tabId ->
            val tab = findViewById<LinearLayout>(tabId)
            val text = tab.getChildAt(1) as android.widget.TextView

            if (tabId == selectedId) {
                text.setTextColor(ContextCompat.getColor(this, R.color.button))
            } else {
                text.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateSelectedTab(R.id.nav_profile)
    }
}