package com.example.smartagro

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {

    // UI components
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        initViews()

        // Set click listener for sign up button
        signUpButton.setOnClickListener {
            if (validateInputs()) {
                // Show success toast
                Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()

                // Navigate to Login Activity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

                // Optional: finish current activity so user can't go back
                finish()
            }
        }
    }

    private fun initViews() {
        usernameEditText = findViewById(R.id.editText_username)
        emailEditText = findViewById(R.id.editText_email)
        passwordEditText = findViewById(R.id.editText_password)
        usernameInputLayout = findViewById(R.id.textInputLayout_username)
        emailInputLayout = findViewById(R.id.textInputLayout_email)
        passwordInputLayout = findViewById(R.id.textInputLayout_password)
        signUpButton = findViewById(R.id.button_signup)
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Clear previous errors
        usernameInputLayout.error = null
        emailInputLayout.error = null
        passwordInputLayout.error = null

        // Get input values
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate username
        if (username.isEmpty()) {
            usernameInputLayout.error = "Username is required"
            isValid = false
        } else if (username.length < 3) {
            usernameInputLayout.error = "Username must be at least 3 characters"
            isValid = false
        }

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Please enter a valid email address"
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }
}