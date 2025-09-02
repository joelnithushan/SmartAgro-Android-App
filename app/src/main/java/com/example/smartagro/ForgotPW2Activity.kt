package com.example.smartagro

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ForgotPW2Activity : AppCompatActivity() {

    // UI components for OTP input fields
    private lateinit var otpField1: TextInputEditText
    private lateinit var otpField2: TextInputEditText
    private lateinit var otpField3: TextInputEditText
    private lateinit var otpField4: TextInputEditText
    private lateinit var otpField5: TextInputEditText
    private lateinit var verifyButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_pw2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        initViews()

        // Set up OTP input behavior
        setupOtpInputs()

        // Set click listener for verify button
        verifyButton.setOnClickListener {
            if (validateOtp()) {
                // Show success toast
                Toast.makeText(this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to ForgotPW3Activity
                val intent = Intent(this, ForgotPW3Activity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun initViews() {
        // Note: You need to add these IDs to your XML TextInputEditText fields
        // otpField1 = findViewById(R.id.editText_otp1)
        // otpField2 = findViewById(R.id.editText_otp2)
        // otpField3 = findViewById(R.id.editText_otp3)
        // otpField4 = findViewById(R.id.editText_otp4)
        // otpField5 = findViewById(R.id.editText_otp5)

        verifyButton = findViewById(R.id.btnOTP2)

        // Temporary solution - you'll need to add IDs to XML first
        try {
            otpField1 = findViewById(R.id.editText_otp1)
            otpField2 = findViewById(R.id.editText_otp2)
            otpField3 = findViewById(R.id.editText_otp3)
            otpField4 = findViewById(R.id.editText_otp4)
            otpField5 = findViewById(R.id.editText_otp5)
        } catch (e: Exception) {
            Toast.makeText(this, "Please add IDs to OTP fields in XML", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupOtpInputs() {
        // Auto-focus next field when current field is filled
        try {
            otpField1.addTextChangedListener(createOtpTextWatcher(otpField1, otpField2))
            otpField2.addTextChangedListener(createOtpTextWatcher(otpField2, otpField3))
            otpField3.addTextChangedListener(createOtpTextWatcher(otpField3, otpField4))
            otpField4.addTextChangedListener(createOtpTextWatcher(otpField4, otpField5))
            otpField5.addTextChangedListener(createOtpTextWatcher(otpField5, null))
        } catch (e: Exception) {
            // Handle case where IDs are not yet added to XML
        }
    }

    private fun createOtpTextWatcher(currentField: TextInputEditText, nextField: TextInputEditText?): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) {
                    nextField?.requestFocus()
                }
            }
        }
    }

    private fun validateOtp(): Boolean {
        try {
            val otp1 = otpField1.text.toString().trim()
            val otp2 = otpField2.text.toString().trim()
            val otp3 = otpField3.text.toString().trim()
            val otp4 = otpField4.text.toString().trim()
            val otp5 = otpField5.text.toString().trim()

            // Check if all fields are filled
            if (otp1.isEmpty() || otp2.isEmpty() || otp3.isEmpty() || otp4.isEmpty() || otp5.isEmpty()) {
                Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show()
                return false
            }

            // Check if all fields contain only digits
            val otpCode = otp1 + otp2 + otp3 + otp4 + otp5
            if (!otpCode.all { it.isDigit() }) {
                Toast.makeText(this, "OTP should contain only numbers", Toast.LENGTH_SHORT).show()
                return false
            }

            return true
        } catch (e: Exception) {
            Toast.makeText(this, "Please complete XML setup first", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}