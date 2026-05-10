package com.mobile.pawpal.features.adoption

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

class AdoptionRequestActivity : AppCompatActivity() {

    private lateinit var tvPetName: TextView
    private lateinit var etAdopterName: EditText
    private lateinit var etContactInfo: EditText
    private lateinit var etReason: EditText
    private lateinit var etNoteToOwner: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adoption_request)

        val petId = intent.getIntExtra("petId", -1)
        val petName = intent.getStringExtra("petName") ?: "this pet"
        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"
        val fullName = prefs.getString("fullName", "") ?: ""

        tvPetName = findViewById(R.id.tvPetName)
        etAdopterName = findViewById(R.id.etAdopterName)
        etContactInfo = findViewById(R.id.etContactInfo)
        etReason = findViewById(R.id.etReason)
        etNoteToOwner = findViewById(R.id.etNoteToOwner)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        tvPetName.text = "Adopt $petName"
        etAdopterName.setText(fullName)

        btnBack.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val adopterName = etAdopterName.text.toString().trim()
            val contactInfo = etContactInfo.text.toString().trim()
            val reason = etReason.text.toString().trim()
            val noteToOwner = etNoteToOwner.text.toString().trim()

            if (adopterName.isEmpty() || contactInfo.isEmpty() || reason.isEmpty()) {
                tvError.text = "Please fill in all required fields."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            submitRequest(token, petId, adopterName, contactInfo, reason, noteToOwner)
        }
    }

    private fun submitRequest(
        token: String,
        petId: Int,
        adopterName: String,
        contactInfo: String,
        reason: String,
        noteToOwner: String
    ) {
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        val body = mutableMapOf(
            "petId" to petId.toString(),
            "adopterName" to adopterName,
            "contactInfo" to contactInfo,
            "reason" to reason
        )
        if (noteToOwner.isNotEmpty()) body["noteToOwner"] = noteToOwner

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.submitAdoptionRequest(token, body)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@AdoptionRequestActivity,
                            "Adoption request submitted!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        tvError.text = response.body()?.error?.message ?: "Failed to submit request."
                        tvError.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    tvError.text = "Connection error."
                    tvError.visibility = View.VISIBLE
                }
            }
        }
    }
}