package com.mobile.pawpal.features.adoption

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.PetDetailData
import com.mobile.pawpal.shared.ReportRequest
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PetDetailActivity : AppCompatActivity() {

    private lateinit var ivPetImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvBreed: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvType: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvNoDescription: TextView
    private lateinit var chipGroupTraits: LinearLayout
    private lateinit var tvNoTraits: TextView
    private lateinit var llHealthCards: LinearLayout
    private lateinit var tvNoHealth: TextView
    private lateinit var tvLocationDetail: TextView
    private lateinit var mapWebView: WebView
    private lateinit var btnAdopt: Button
    private lateinit var tvRequestDeclined: TextView
    private lateinit var tvAlreadyRequested: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: TextView

    private var petOwnerId: Int = -1
    private var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_detail)

        val petId = intent.getIntExtra("petId", -1)
        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"

        ivPetImage = findViewById(R.id.ivPetImage)
        tvName = findViewById(R.id.tvName)
        tvBreed = findViewById(R.id.tvBreed)
        tvAge = findViewById(R.id.tvAge)
        tvType = findViewById(R.id.tvType)
        tvGender = findViewById(R.id.tvGender)
        tvLocation = findViewById(R.id.tvLocation)
        tvDescription = findViewById(R.id.tvDescription)
        tvNoDescription = findViewById(R.id.tvNoDescription)
        chipGroupTraits = findViewById(R.id.chipGroupTraits)
        tvNoTraits = findViewById(R.id.tvNoTraits)
        llHealthCards = findViewById(R.id.llHealthCards)
        tvNoHealth = findViewById(R.id.tvNoHealth)
        tvLocationDetail = findViewById(R.id.tvLocationDetail)
        mapWebView = findViewById(R.id.mapWebView)
        btnAdopt = findViewById(R.id.btnAdopt)
        tvRequestDeclined = findViewById(R.id.tvRequestDeclined)
        tvAlreadyRequested = findViewById(R.id.tvAlreadyRequested)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        if (petId == -1) { finish(); return }

        loadPetDetail(token, petId)
    }

    private fun loadPetDetail(token: String, petId: Int) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val petResponse = RetrofitClient.instance.getPetById(token, petId)
                val myRequestsResponse = RetrofitClient.instance.getMyRequests(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (petResponse.isSuccessful && petResponse.body()?.success == true) {
                        val pet = petResponse.body()!!.data!!
                        val myRequests = myRequestsResponse.body()?.data ?: emptyList()
                        val myRequestsForThisPet = myRequests.filter { it.pet.id == petId }
                        val declineCount = myRequestsForThisPet.count {
                            it.status.uppercase() == "DECLINED" || it.status.uppercase() == "REJECTED"
                        }
                        val latestRequest = myRequestsForThisPet.maxByOrNull { it.id }
                        bindPet(pet, latestRequest?.status, latestRequest?.declineReason, declineCount)
                    } else {
                        Toast.makeText(this@PetDetailActivity, "Failed to load pet.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@PetDetailActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun bindPet(pet: PetDetailData, myRequestStatus: String?, declineReason: String?, declineCount: Int) {
        tvName.text = pet.name
        tvBreed.text = pet.breed
        tvAge.text = "🕐 ${pet.age}"
        tvType.text = "🐾 ${pet.type}"
        tvGender.text = "⚥ ${pet.gender ?: "Unknown"}"
        tvLocation.text = "📍 ${pet.location}"
        tvLocationDetail.text = pet.location

        petOwnerId = pet.owner?.id ?: -1

        if (!pet.imageUrl.isNullOrEmpty()) {
            val fullUrl = if (pet.imageUrl.startsWith("http")) pet.imageUrl
            else "https://net-vanquish-poise.ngrok-free.dev${pet.imageUrl}"
            Glide.with(this).load(fullUrl).centerCrop().into(ivPetImage)
        }

        if (!pet.description.isNullOrBlank()) {
            tvDescription.text = pet.description
            tvDescription.visibility = View.VISIBLE
            tvNoDescription.visibility = View.GONE
        } else {
            tvDescription.visibility = View.GONE
            tvNoDescription.visibility = View.VISIBLE
        }

        val traits = pet.characteristics ?: emptyList()
        if (traits.isNotEmpty()) {
            chipGroupTraits.removeAllViews()
            tvNoTraits.visibility = View.GONE
            traits.forEach { trait ->
                val chip = TextView(this).apply {
                    text = trait
                    textSize = 13f
                    setPadding(28, 12, 28, 12)
                    setTextColor(getColor(R.color.green))
                    setBackgroundResource(R.drawable.chip_inactive)
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.marginEnd = 8
                    lp.bottomMargin = 8
                    layoutParams = lp
                }
                chipGroupTraits.addView(chip)
            }
        } else {
            tvNoTraits.visibility = View.VISIBLE
        }

        val healthItems = mutableListOf<Pair<String, String>>()
        if (pet.vaccinated) healthItems.add(Pair("Vaccinated", "Up to date on all shots"))
        if (pet.neutered) healthItems.add(Pair("Neutered", "Spayed/neutered"))
        if (pet.microchipped) healthItems.add(Pair("Microchipped", "ID chip implanted"))
        if (pet.healthChecked) healthItems.add(Pair("Health Checked", "Vet examined"))

        if (healthItems.isNotEmpty()) {
            llHealthCards.removeAllViews()
            tvNoHealth.visibility = View.GONE
            healthItems.forEach { (title, subtitle) ->
                val card = layoutInflater.inflate(R.layout.item_health_card, llHealthCards, false)
                card.findViewById<TextView>(R.id.tvHealthTitle).text = title
                card.findViewById<TextView>(R.id.tvHealthSubtitle).text = subtitle
                llHealthCards.addView(card)
            }
        } else {
            tvNoHealth.visibility = View.VISIBLE
        }

        if (pet.latitude != null && pet.longitude != null) {
            loadMap(pet.latitude, pet.longitude)
        } else {
            mapWebView.visibility = View.GONE
        }

        // Report button
        findViewById<TextView>(R.id.btnReport)?.setOnClickListener {
            if (petOwnerId == -1) return@setOnClickListener
            showReportDialog(petOwnerId)
        }

        when {
            declineCount >= 3 -> {
                btnAdopt.visibility = View.GONE
                tvAlreadyRequested.visibility = View.GONE
                tvRequestDeclined.visibility = View.VISIBLE
                tvRequestDeclined.text = "✕  You are no longer eligible to adopt this pet."
            }
            myRequestStatus != null -> {
                when (myRequestStatus.uppercase()) {
                    "PENDING" -> {
                        btnAdopt.visibility = View.GONE
                        tvAlreadyRequested.visibility = View.VISIBLE
                        tvRequestDeclined.visibility = View.GONE
                    }
                    "APPROVED" -> {
                        btnAdopt.visibility = View.GONE
                        tvAlreadyRequested.visibility = View.GONE
                        tvRequestDeclined.visibility = View.VISIBLE
                        tvRequestDeclined.text = "✓  Your adoption request was approved!"
                    }
                    "DECLINED", "REJECTED" -> {
                        btnAdopt.visibility = View.VISIBLE
                        tvAlreadyRequested.visibility = View.GONE
                        tvRequestDeclined.visibility = View.VISIBLE
                        tvRequestDeclined.text = if (!declineReason.isNullOrBlank())
                            "✕  Declined: $declineReason"
                        else "✕  Your request was not approved."
                        btnAdopt.text = "↺  Try Again"
                        btnAdopt.setOnClickListener {
                            val intent = Intent(this, AdoptionRequestActivity::class.java)
                            intent.putExtra("petId", pet.id)
                            intent.putExtra("petName", pet.name)
                            startActivity(intent)
                        }
                    }
                    else -> {
                        btnAdopt.visibility = View.VISIBLE
                        tvAlreadyRequested.visibility = View.GONE
                        tvRequestDeclined.visibility = View.GONE
                    }
                }
            }
            pet.status.uppercase() == "AVAILABLE" -> {
                btnAdopt.visibility = View.VISIBLE
                tvRequestDeclined.visibility = View.GONE
                tvAlreadyRequested.visibility = View.GONE
                btnAdopt.setOnClickListener {
                    val intent = Intent(this, AdoptionRequestActivity::class.java)
                    intent.putExtra("petId", pet.id)
                    intent.putExtra("petName", pet.name)
                    startActivity(intent)
                }
            }
            else -> {
                btnAdopt.visibility = View.GONE
                tvAlreadyRequested.visibility = View.GONE
                tvRequestDeclined.visibility = View.VISIBLE
                tvRequestDeclined.text = "✕  This pet is no longer available."
            }
        }
    }

    private fun showReportDialog(reportedUserId: Int) {
        val reasons = listOf(
            "Fake or misleading listing",
            "Suspected animal abuse or neglect",
            "Inappropriate content",
            "Suspicious or scam behavior",
            "Others"
        )
        val selectedReasons = mutableSetOf<String>()
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 32, 48, 16) }
        val scrollView = android.widget.ScrollView(this)
        val reasonsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val customInput = android.widget.EditText(this).apply {
            hint = "Describe your reason..."; setPadding(24, 16, 24, 16); visibility = View.GONE
            setBackgroundResource(R.drawable.chip_inactive); layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).also { it.topMargin = 8 }
        }
        reasons.forEach { reason ->
            val chip = android.widget.TextView(this).apply {
                text = reason; setPadding(32, 20, 32, 20); textSize = 14f
                setBackgroundResource(R.drawable.chip_inactive); setTextColor(getColor(R.color.dark))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, 8, 0, 8) }
            }
            chip.setOnClickListener {
                if (selectedReasons.contains(reason)) {
                    selectedReasons.remove(reason)
                    chip.setBackgroundResource(R.drawable.chip_inactive)
                    chip.setTextColor(getColor(R.color.dark))
                } else {
                    selectedReasons.add(reason)
                    chip.setBackgroundResource(R.drawable.chip_active)
                    chip.setTextColor(getColor(R.color.white))
                }
                if (reason == "Others") customInput.visibility =
                    if (selectedReasons.contains("Others")) View.VISIBLE else View.GONE
            }
            reasonsContainer.addView(chip)
        }
        reasonsContainer.addView(customInput)
        scrollView.addView(reasonsContainer)
        dialogView.addView(scrollView)
        android.app.AlertDialog.Builder(this)
            .setTitle("Report this owner")
            .setMessage("Select one or more reasons:")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val finalReasons = selectedReasons.filter { it != "Others" }.toMutableList()
                if (selectedReasons.contains("Others") && customInput.text.toString().trim().isNotEmpty())
                    finalReasons.add(customInput.text.toString().trim())
                if (finalReasons.isEmpty()) {
                    Toast.makeText(this, "Please select at least one reason.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                submitReport(reportedUserId, finalReasons.joinToString("; "))
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun submitReport(reportedUserId: Int, reason: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = ReportRequest(
                    reportedUserId = reportedUserId,
                    reason = reason
                )

                val response = RetrofitClient.instance.submitReport(token, body)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(
                            this@PetDetailActivity,
                            "Report submitted. Thank you!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val errorText = response.errorBody()?.string()

                        Toast.makeText(
                            this@PetDetailActivity,
                            "Failed to submit report: $errorText",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PetDetailActivity,
                        "Connection error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadMap(latitude: Double, longitude: Double) {
        mapWebView.visibility = View.VISIBLE
        val settings: WebSettings = mapWebView.settings
        settings.javaScriptEnabled = true
        val html = """
            <!DOCTYPE html><html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>body{margin:0;padding:0;}#map{width:100%;height:220px;}</style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([$latitude, $longitude], 13);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'© OpenStreetMap'}).addTo(map);
                    L.marker([$latitude, $longitude]).addTo(map);
                </script>
            </body></html>
        """.trimIndent()
        mapWebView.loadDataWithBaseURL("https://unpkg.com", html, "text/html", "UTF-8", null)
    }
}