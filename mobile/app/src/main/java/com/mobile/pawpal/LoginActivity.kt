package com.mobile.pawpal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: LinearLayout
    private lateinit var tvRegister: TextView
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegister = findViewById(R.id.tvRegister)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                showError("Please enter your email and password.")
                return@setOnClickListener
            }
            login(email, password)
        }

        btnGoogle.setOnClickListener {
            startActivity(Intent(this, GoogleAuthActivity::class.java))
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun login(email: String, password: String) {
        tvError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!.data!!
                        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("token", data.accessToken)
                            .putString("fullName", data.user.fullName)
                            .putString("email", data.user.email)
                            .putString("role", data.user.role)
                            .apply()
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        showError(response.body()?.error?.message ?: "Invalid email or password.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    showError("Connection failed. Is your server running?")
                }
            }
        }
    }
}