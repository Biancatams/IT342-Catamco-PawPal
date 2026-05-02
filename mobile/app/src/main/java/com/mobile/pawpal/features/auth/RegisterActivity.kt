package com.mobile.pawpal.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.RegisterRequest
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var rgRole: RadioGroup
    private lateinit var rbAdopter: RadioButton
    private lateinit var rbPetOwner: RadioButton
    private lateinit var btnRegister: Button
    private lateinit var btnGoogle: LinearLayout
    private lateinit var tvLogin: TextView
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        rgRole = findViewById(R.id.rgRole)
        rbAdopter = findViewById(R.id.rbAdopter)
        rbPetOwner = findViewById(R.id.rbPetOwner)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvLogin = findViewById(R.id.tvLogin)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)

        rbAdopter.setOnClickListener {
            rbAdopter.setBackgroundResource(R.drawable.role_button_active)
            rbAdopter.setTextColor(getColor(R.color.white))
            rbPetOwner.setBackgroundResource(R.drawable.role_button_inactive)
            rbPetOwner.setTextColor(getColor(R.color.muted))
        }

        rbPetOwner.setOnClickListener {
            rbPetOwner.setBackgroundResource(R.drawable.role_button_active)
            rbPetOwner.setTextColor(getColor(R.color.white))
            rbAdopter.setBackgroundResource(R.drawable.role_button_inactive)
            rbAdopter.setTextColor(getColor(R.color.muted))
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val role = when (rgRole.checkedRadioButtonId) {
                R.id.rbAdopter -> "ADOPTER"
                R.id.rbPetOwner -> "PET_OWNER"
                else -> ""
            }

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showError("Please fill in all fields.")
                return@setOnClickListener
            }
            if (password.length < 8) {
                showError("Password must be at least 8 characters.")
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                showError("Passwords do not match.")
                return@setOnClickListener
            }
            if (role.isEmpty()) {
                showError("Please select a role.")
                return@setOnClickListener
            }
            register(fullName, email, password, confirmPassword, role)
        }

        btnGoogle.setOnClickListener {
            startActivity(Intent(this, GoogleAuthActivity::class.java))
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun register(fullName: String, email: String, password: String, confirmPassword: String, role: String) {
        tvError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.register(
                    RegisterRequest(fullName, email, password, confirmPassword, role)
                )
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!.data!!
                        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("token", data.accessToken)
                            .putString("fullName", data.user.fullName)
                            .putString("email", data.user.email)
                            .putString("role", data.user.role)
                            .apply()
                        startActivity(Intent(this@RegisterActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        showError(response.body()?.error?.message ?: "Registration failed.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnRegister.isEnabled = true
                    showError("Connection failed. Is your server running?")
                }
            }
        }
    }
}