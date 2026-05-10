package com.mobile.pawpal.features.verification

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerificationStatusActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvAdminComment: TextView
    private lateinit var btnResubmit: Button
    private lateinit var progressBar: ProgressBar

    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_status)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"

        tvStatus = findViewById(R.id.tvStatus)
        tvMessage = findViewById(R.id.tvMessage)
        tvAdminComment = findViewById(R.id.tvAdminComment)
        btnResubmit = findViewById(R.id.btnResubmit)
        progressBar = findViewById(R.id.progressBar)

        loadStatus()

        // --- NEW BUTTON LOGIC: CANCEL & RETURN HOME ---
        val btnCancelHome = findViewById<Button>(R.id.btnCancelHome)
        if (btnCancelHome != null) {
            btnCancelHome.setOnClickListener {
                getSharedPreferences("pawpal_prefs", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this, com.mobile.pawpal.features.auth.LandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadStatus()
    }

    private fun loadStatus() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getMyVerification(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!.data
                        val status = data?.status ?: "NONE"

                        when (status.uppercase()) {
                            "PENDING" -> {
                                tvStatus.text = "⏳ Verification Pending"
                                tvStatus.setBackgroundResource(R.drawable.badge_pending)
                                tvMessage.text = "Your verification request is being reviewed by our admin. Please wait."
                                tvAdminComment.visibility = View.GONE
                                btnResubmit.visibility = View.GONE
                            }
                            "APPROVED" -> {
                                tvStatus.text = "✓ Verified"
                                tvStatus.setBackgroundResource(R.drawable.badge_available)
                                tvMessage.text = "Your account is verified! You can now use all features."
                                tvAdminComment.visibility = View.GONE
                                btnResubmit.visibility = View.GONE
                                routeToDashboard()
                            }
                            "REJECTED" -> {
                                tvStatus.text = "✕ Verification Rejected"
                                tvStatus.setBackgroundResource(R.drawable.badge_declined)
                                tvMessage.text = "Your verification was rejected."
                                val comment = data?.adminComment
                                if (!comment.isNullOrBlank()) {
                                    tvAdminComment.visibility = View.VISIBLE
                                    tvAdminComment.text = "Admin note: $comment"
                                } else {
                                    tvAdminComment.visibility = View.GONE
                                }
                                btnResubmit.visibility = View.VISIBLE
                                btnResubmit.setOnClickListener {
                                    startActivity(Intent(this@VerificationStatusActivity, VerificationActivity::class.java))
                                }
                            }
                            else -> {
                                startActivity(Intent(this@VerificationStatusActivity, VerificationActivity::class.java))
                                finish()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun routeToDashboard() {
        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val role = prefs.getString("role", "") ?: ""
        val intent = when (role.uppercase()) {
            "PET_OWNER" -> Intent(this, com.mobile.pawpal.features.pets.OwnerDashboardActivity::class.java)
            else -> Intent(this, com.mobile.pawpal.features.adoption.AdopterDashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}