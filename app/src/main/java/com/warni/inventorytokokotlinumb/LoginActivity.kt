// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/LoginActivity.kt
package com.warni.inventorytokokotlinumb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.warni.inventorytokokotlinumb.models.LoginRequest
import com.warni.inventorytokokotlinumb.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log // Import Log

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var loadingProgressBar: ProgressBar

    // Tag untuk Logcat
    private val TAG = "LoginActivityDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        // Check if user is already logged in (has a token in SharedPreferences)
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPref.getString("auth_token", null)
        val userRole = sharedPref.getString("user_role", null)
        val userId = sharedPref.getInt("user_id", -1)
        val username = sharedPref.getString("username", null)

        // --- Log untuk debugging SharedPreferences ---
        Log.d(TAG, "onCreate: Checking SharedPreferences for existing session.")
        Log.d(TAG, "onCreate: authToken = $authToken")
        Log.d(TAG, "onCreate: userRole = $userRole")
        Log.d(TAG, "onCreate: userId = $userId")
        Log.d(TAG, "onCreate: username = $username")
        // --- Akhir Log debugging ---

        if (authToken != null && userRole != null && userId != -1 && username != null) {
            Log.d(TAG, "onCreate: Existing session found. Navigating to MainActivity.")
            // If token exists, set the token in RetrofitClient and navigate to MainActivity
            RetrofitClient.setAuthToken(authToken) // PENTING: Set ulang token di RetrofitClient
            navigateToMain()
            return // Important: Stop onCreate execution to prevent showing login UI
        } else {
            Log.d(TAG, "onCreate: No valid existing session found. Staying on LoginActivity.")
        }

        loginButton.setOnClickListener {
            performLogin()
        }

        registerTextView.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun performLogin() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        loadingProgressBar.visibility = ProgressBar.VISIBLE
        loginButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = LoginRequest(username, password)
                val response = RetrofitClient.instance.loginUser(request)

                withContext(Dispatchers.Main) {
                    loadingProgressBar.visibility = ProgressBar.GONE
                    loginButton.isEnabled = true

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        loginResponse?.let {
                            // Save token, role, and user ID to SharedPreferences
                            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                // Menggunakan operator Elvis (?:) untuk memastikan userId non-nullable
                                putInt("user_id", it.userId ?: -1) // Memberikan nilai default -1 jika userId null
                                putString("auth_token", it.token)
                                putString("user_role", it.role)
                                putString("username", username) // Simpan username juga
                                apply()
                            }

                            // Set the token in RetrofitClient for subsequent API calls
                            RetrofitClient.setAuthToken(it.token)
                            Log.d(TAG, "performLogin: Login successful. Token and user data saved.")

                            Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
                            navigateToMain()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "performLogin: Login failed: $errorBody")
                        Toast.makeText(this@LoginActivity, "Login gagal: ${errorBody}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingProgressBar.visibility = ProgressBar.GONE
                    loginButton.isEnabled = true
                    Log.e(TAG, "performLogin: Network error during login: ${e.message}", e)
                    Toast.makeText(this@LoginActivity, "Terjadi kesalahan jaringan: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}
