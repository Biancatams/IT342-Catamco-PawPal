package com.mobile.pawpal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class GoogleAuthActivity : AppCompatActivity() {

    private val clientId = "YOUR_GOOGLE_CLIENT_ID" // replace with your actual client ID from web frontend
    private val redirectUri = "http://10.0.2.2:8080/api/v1/auth/google-login"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        setContentView(webView)

        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=$clientId" +
                "&redirect_uri=urn:ietf:wg:oauth:2.0:oob" +
                "&response_type=token" +
                "&scope=openid%20email%20profile"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                url?.let {
                    if (it.contains("access_token=")) {
                        val token = it.substringAfter("access_token=").substringBefore("&")
                        webView.stopLoading()
                        handleGoogleToken(token)
                    }
                }
            }
        }

        webView.loadUrl(authUrl)
    }

    private fun handleGoogleToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.googleLogin(
                    GoogleAuthRequest(token)
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
                            .apply()
                        startActivity(Intent(this@GoogleAuthActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@GoogleAuthActivity, "Google login failed.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GoogleAuthActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}