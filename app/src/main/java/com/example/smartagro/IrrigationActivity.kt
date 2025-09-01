package com.example.smartagro

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class IrrigationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_irrigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        updateSelectedTab(R.id.nav_irrigation)
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
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            updateSelectedTab(R.id.nav_profile)
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
        updateSelectedTab(R.id.nav_irrigation)
    }
}