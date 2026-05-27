package com.mobile.pawpal.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.mobile.pawpal.features.admin.AdminDashboardActivity
import com.mobile.pawpal.features.adoption.AdopterDashboardActivity
import com.mobile.pawpal.features.pets.OwnerDashboardActivity
import com.mobile.pawpal.features.verification.VerificationStatusActivity
import com.mobile.pawpal.shared.GoogleAuthRequest
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleAuthActivity : AppCompatActivity() {

    private val WEB_CLIENT_ID = "46076906009-33vp9geqsngv3poo133m1ieppnc0qpj6.apps.googleusercontent.com"

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            val authCode = account.serverAuthCode
            val tokenToSend = idToken ?: authCode
            if (tokenToSend != null) {
                sendTokenToBackend(tokenToSend)
            } else {
                Toast.makeText(this, "Google Sign-In failed: no token.", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In error: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestServerAuthCode(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            signInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun sendTokenToBackend(idToken: String) {
        val mode = intent.getStringExtra("mode") ?: "login"
        val role = intent.getStringExtra("role")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (mode == "register") {
                    RetrofitClient.instance.googleRegister(GoogleAuthRequest(token = idToken, role = role))
                } else {
                    RetrofitClient.instance.googleLogin(GoogleAuthRequest(token = idToken))
                }
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!.data!!
                        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("token", data.accessToken)
                            .putString("fullName", data.user.fullName)
                            .putString("email", data.user.email)
                            .putString("role", data.user.role)
                            .apply()

                        val userRole = data.user.role.uppercase()

                        if (userRole == "ADMIN") {
                            val intent = Intent(this@GoogleAuthActivity, AdminDashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                            return@withContext
                        }

                        if (mode == "register") {
                            val intent = Intent(this@GoogleAuthActivity, VerificationStatusActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                            return@withContext
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val verifToken = "Bearer ${data.accessToken}"
                                val verifResponse = RetrofitClient.instance.getMyVerification(verifToken)
                                withContext(Dispatchers.Main) {
                                    val status = verifResponse.body()?.data?.status?.uppercase() ?: "NONE"
                                    val intent = if (status == "APPROVED") {
                                        when (userRole) {
                                            "PET_OWNER" -> Intent(this@GoogleAuthActivity, OwnerDashboardActivity::class.java)
                                            else -> Intent(this@GoogleAuthActivity, AdopterDashboardActivity::class.java)
                                        }
                                    } else {
                                        Intent(this@GoogleAuthActivity, VerificationStatusActivity::class.java)
                                    }
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    val intent = Intent(this@GoogleAuthActivity, VerificationStatusActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@GoogleAuthActivity,
                            response.body()?.error?.message ?: "Google login failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GoogleAuthActivity, "Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}