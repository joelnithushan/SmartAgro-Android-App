package com.example.smartagro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val linkNavigate1: TextView = findViewById(R.id.loginLink1)
        linkNavigate1.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        val linkNavigate2: TextView = findViewById(R.id.loginLink2)
        linkNavigate2.setOnClickListener {
            val intent = Intent(this, ForgotPW1Activity::class.java)
            startActivity(intent)
        }

        val buttonNavigate1: Button = findViewById(R.id.btnLogin)
        buttonNavigate1.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        val buttonNavigate2: Button = findViewById(R.id.btnGoogle)
        buttonNavigate2.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        val buttonNavigate3: Button = findViewById(R.id.btnApple)
        buttonNavigate3.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }
}