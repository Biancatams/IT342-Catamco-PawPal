package com.mobile.pawpal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)
        btnLogout = findViewById(R.id.btnLogout)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "User")
        val email = prefs.getString("email", "")
        val role = prefs.getString("role", "")

        tvWelcome.text = "Welcome, $fullName! 🐾"
        tvEmail.text = email
        tvRole.text = if (role == "PET_OWNER") "Pet Owner" else "Adopter"

        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}