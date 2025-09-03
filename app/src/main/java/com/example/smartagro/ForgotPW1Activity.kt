package com.example.smartagro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPW1Activity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var sendOtpButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_pw1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        sendOtpButton.setOnClickListener {
            Toast.makeText(this, "OTP sent to your email!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ForgotPW2Activity::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.editText_email)
        emailInputLayout = findViewById(R.id.textInputLayout_email)
        sendOtpButton = findViewById(R.id.btnOTP1)
    }
}