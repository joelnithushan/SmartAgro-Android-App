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

class ForgotPW3Activity : AppCompatActivity() {

    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var newPasswordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var updatePasswordButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_pw3)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        updatePasswordButton.setOnClickListener {
            Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ForgotPW4Activity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        newPasswordEditText = findViewById(R.id.passInput2)
        confirmPasswordEditText = findViewById(R.id.editText_password)
        newPasswordInputLayout = findViewById(R.id.passInput1)
        confirmPasswordInputLayout = findViewById(R.id.textInputLayout_password)
        updatePasswordButton = findViewById(R.id.button_signup4)
    }
}