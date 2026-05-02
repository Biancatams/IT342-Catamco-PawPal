package com.mobile.pawpal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
            if (idToken != null) {
                sendTokenToBackend(idToken)
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
            .requestEmail()
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        // Always force account picker
        googleSignInClient.signOut().addOnCompleteListener {
            signInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun sendTokenToBackend(idToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.googleLogin(
                    GoogleAuthRequest(idToken)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!.data!!
                        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("token", data.accessToken)
                            .putString("fullName", data.user.fullName)
                            .putString("email", data.user.email)
                            .putString("role", data.user.role)
                            .putInt("userId", data.user.id)
                            .apply()
                        startActivity(Intent(this@GoogleAuthActivity, DashboardActivity::class.java))
                        finish()
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