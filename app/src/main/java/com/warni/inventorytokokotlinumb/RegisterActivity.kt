// File: app/src/main/java/com/example/inventorytokokotlinumb/RegisterActivity.kt
package com.warni.inventorytokokotlinumb

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.warni.inventorytokokotlinumb.models.RegisterRequest
import com.warni.inventorytokokotlinumb.network.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var backToLoginText: TextView
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Views
        usernameInput = findViewById(R.id.usernameRegisterInput)
        passwordInput = findViewById(R.id.passwordRegisterInput)
        registerButton = findViewById(R.id.registerButton)
        backToLoginText = findViewById(R.id.backToLoginText)
        loadingProgressBar = findViewById(R.id.loadingRegisterProgressBar)

        registerButton.setOnClickListener {
            performRegister()
        }

        backToLoginText.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }

    private fun performRegister() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        loadingProgressBar.visibility = View.VISIBLE
        registerButton.isEnabled = false // Disable button while loading

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.registerUser(RegisterRequest(username, password))

                withContext(Dispatchers.Main) {
                    loadingProgressBar.visibility = View.GONE
                    registerButton.isEnabled = true

                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        Toast.makeText(this@RegisterActivity, registerResponse?.message ?: "Registration successful!", Toast.LENGTH_SHORT).show()
                        finish() // Go back to LoginActivity after successful registration
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = try {
                            // Try to parse the error body as JSON to get the error message from the backend
                            // This requires Gson on the client side for JSON parsing
                            val errorResponse = com.google.gson.Gson().fromJson(errorBody, com.warni.inventorytokokotlinumb.models.ApiResponse::class.java)
                            errorResponse?.message ?: "Registration failed: ${response.code()}"
                        } catch (e: Exception) {
                            "Registration failed: ${response.code()} - ${errorBody}"
                        }
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingProgressBar.visibility = View.GONE
                    registerButton.isEnabled = true
                    Toast.makeText(this@RegisterActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
