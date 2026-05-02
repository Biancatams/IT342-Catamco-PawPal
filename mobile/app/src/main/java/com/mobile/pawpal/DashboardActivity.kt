package com.mobile.pawpal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val role = prefs.getString("role", "")
        when (role) {
            "ADOPTER" -> {
                startActivity(Intent(this, AdopterDashboardActivity::class.java))
            }
            "PET_OWNER" -> {
                startActivity(Intent(this, OwnerDashboardActivity::class.java))
            }
            "ADMIN" -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
            }
            else -> {
                startActivity(Intent(this, LandingActivity::class.java))
            }
        }
        finish()
    }
}