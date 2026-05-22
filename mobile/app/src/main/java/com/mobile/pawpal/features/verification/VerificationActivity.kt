package com.mobile.pawpal.features.verification

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class VerificationActivity : AppCompatActivity() {

    private lateinit var ivIdPreview: ImageView
    private lateinit var tvPickId: TextView
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var spinnerLocation: Spinner
    private lateinit var etReason: EditText
    private lateinit var btnSubmit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvPhonePrefix: TextView

    private var selectedImageUri: Uri? = null
    private var token = ""
    private var isUpdatingPhone = false

    private val locations = listOf(
        "Select your city/municipality",
        "Cebu City", "Mandaue", "Lapu-Lapu", "Talisay", "Danao", "Carcar",
        "Toledo", "Naga", "Bogo", "Minglanilla", "San Fernando", "Consolacion",
        "Liloan", "Compostela", "Cordova", "Moalboal", "Oslob", "Alcoy",
        "Dalaguete", "Others"
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            ivIdPreview.setImageURI(selectedImageUri)
            ivIdPreview.visibility = View.VISIBLE
            tvPickId.text = "✓  ID image selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"
        val role = prefs.getString("role", "") ?: ""
        val savedFullName = prefs.getString("fullName", "") ?: ""

        ivIdPreview = findViewById(R.id.ivIdPreview)
        tvPickId = findViewById(R.id.tvPickId)
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        etReason = findViewById(R.id.etReason)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvRole = findViewById(R.id.tvRole)
        tvPhonePrefix = findViewById(R.id.tvPhonePrefix)

        etFullName.setText(savedFullName)

        tvRole.text = if (role.uppercase() == "PET_OWNER")
            "As a Pet Owner, you need to verify your identity before posting pets."
        else
            "As an Adopter, you need to verify your identity before sending adoption requests."

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLocation.adapter = adapter

        setupPhoneField()

        findViewById<LinearLayout>(R.id.llPickId).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnSubmit.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val phoneDigits = etPhone.text.toString().trim()
            val locationIndex = spinnerLocation.selectedItemPosition
            val reason = etReason.text.toString().trim()

            if (fullName.isEmpty()) { showError("Please enter your full name."); return@setOnClickListener }
            if (phoneDigits.length != 10) { showError("Please enter a valid 10-digit phone number after +63."); return@setOnClickListener }
            if (locationIndex == 0) { showError("Please select your location."); return@setOnClickListener }
            if (selectedImageUri == null) { showError("Please upload a valid government-issued ID."); return@setOnClickListener }
            if (reason.isEmpty()) { showError("Please provide a reason for verification."); return@setOnClickListener }

            val fullPhone = "+63$phoneDigits"
            submitVerification(fullName, fullPhone, locations[locationIndex], reason)
        }

        val btnCancelHome = findViewById<Button>(R.id.btnCancelHome)
        if (btnCancelHome != null) {
            btnCancelHome.setOnClickListener {
                val intent = Intent(this, com.mobile.pawpal.features.auth.LandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupPhoneField() {
        etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingPhone) return
                isUpdatingPhone = true
                val digits = s.toString().filter { it.isDigit() }
                val limited = if (digits.length > 10) digits.substring(0, 10) else digits
                if (s.toString() != limited) {
                    etPhone.setText(limited)
                    etPhone.setSelection(limited.length)
                }
                isUpdatingPhone = false
            }
        })
    }

    private fun submitVerification(fullName: String, phone: String, location: String, reason: String) {
        tvError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uri = selectedImageUri!!
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "id_upload_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { output -> inputStream?.copyTo(output) }

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("idImage", file.name, requestFile)
                val reasonBody = reason.toRequestBody("text/plain".toMediaTypeOrNull())
                val fullNameBody = fullName.toRequestBody("text/plain".toMediaTypeOrNull())
                val phoneBody = phone.toRequestBody("text/plain".toMediaTypeOrNull())
                val locationBody = location.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitClient.instance.submitVerification(
                    token, reasonBody, imagePart, fullNameBody, phoneBody, locationBody
                )

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@VerificationActivity,
                            "Verification submitted! Please wait for admin approval.",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@VerificationActivity, VerificationStatusActivity::class.java))
                        finish()
                    } else {
                        showError(response.body()?.error?.message ?: "Submission failed.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    showError("Connection error.")
                }
            }
        }
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }
}