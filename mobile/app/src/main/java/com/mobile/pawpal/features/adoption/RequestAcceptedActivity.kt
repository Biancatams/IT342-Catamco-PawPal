package com.mobile.pawpal.features.adoption

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobile.pawpal.R

class RequestAcceptedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_accepted)

        val petName = intent.getStringExtra("petName") ?: ""
        val petBreed = intent.getStringExtra("petBreed") ?: ""
        val petAge = intent.getStringExtra("petAge") ?: ""
        val petImageUrl = intent.getStringExtra("petImageUrl") ?: ""
        val ownerName = intent.getStringExtra("ownerName") ?: ""
        val ownerPhone = intent.getStringExtra("ownerPhone") ?: ""
        val ownerEmail = intent.getStringExtra("ownerEmail") ?: ""
        val ownerImageUrl = intent.getStringExtra("ownerImageUrl") ?: ""

        val ivPet = findViewById<ImageView>(R.id.ivPetImage)
        val tvPetName = findViewById<TextView>(R.id.tvPetName)
        val tvPetBreed = findViewById<TextView>(R.id.tvPetBreed)
        val tvPetAge = findViewById<TextView>(R.id.tvPetAge)
        val tvOwnerName = findViewById<TextView>(R.id.tvOwnerName)
        val ivOwner = findViewById<ImageView>(R.id.ivOwnerImage)
        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val btnCopyPhone = findViewById<ImageButton>(R.id.btnCopyPhone)
        val btnCopyEmail = findViewById<ImageButton>(R.id.btnCopyEmail)
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnBackToBrowse = findViewById<Button>(R.id.btnBackToBrowse)

        tvPetName.text = petName
        tvPetBreed.text = petBreed
        tvPetAge.text = "🕐 $petAge"
        tvOwnerName.text = ownerName
        tvPhone.text = ownerPhone
        tvEmail.text = ownerEmail

        if (petImageUrl.isNotEmpty()) {
            val fullUrl = if (petImageUrl.startsWith("http")) petImageUrl
            else "https://net-vanquish-poise.ngrok-free.dev$petImageUrl"
            Glide.with(this).load(fullUrl).centerCrop().into(ivPet)
        }

        if (ownerImageUrl.isNotEmpty()) {
            val fullUrl = if (ownerImageUrl.startsWith("http")) ownerImageUrl
            else "https://net-vanquish-poise.ngrok-free.dev$ownerImageUrl"
            Glide.with(this).load(fullUrl).circleCrop().into(ivOwner)
        }

        btnCopyPhone.setOnClickListener {
            copyToClipboard("Phone", ownerPhone)
        }

        btnCopyEmail.setOnClickListener {
            copyToClipboard("Email", ownerEmail)
        }

        btnBack.setOnClickListener { finish() }

        btnBackToBrowse.setOnClickListener {
            startActivity(Intent(this, AdopterDashboardActivity::class.java))
            finish()
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(this, "$label copied!", Toast.LENGTH_SHORT).show()
    }
}