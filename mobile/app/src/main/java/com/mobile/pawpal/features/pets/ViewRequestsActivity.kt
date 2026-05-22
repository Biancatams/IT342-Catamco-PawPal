package com.mobile.pawpal.features.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.OwnerAdoptionRequest
import com.mobile.pawpal.shared.ReportRequest
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewRequestsActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnBack: TextView
    private lateinit var llRequests: LinearLayout
    private lateinit var ivPetImage: ImageView
    private lateinit var tvPetName: TextView
    private lateinit var tvPetBreed: TextView
    private lateinit var tvPetAge: TextView
    private lateinit var tvPetType: TextView
    private lateinit var tvPetLocation: TextView

    private var token = ""
    private var petId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_requests)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"
        petId = intent.getIntExtra("petId", -1)

        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        llRequests = findViewById(R.id.llRequests)
        ivPetImage = findViewById(R.id.ivPetImage)
        tvPetName = findViewById(R.id.tvPetName)
        tvPetBreed = findViewById(R.id.tvPetBreed)
        tvPetAge = findViewById(R.id.tvPetAge)
        tvPetType = findViewById(R.id.tvPetType)
        tvPetLocation = findViewById(R.id.tvPetLocation)

        btnBack.setOnClickListener { finish() }
        loadPetAndRequests()
    }

    private fun loadPetAndRequests() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val petResponse = RetrofitClient.instance.getPetById(token, petId)
                val requestResponse = RetrofitClient.instance.getRequestsForPet(token, petId)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (petResponse.isSuccessful && petResponse.body()?.success == true) {
                        val pet = petResponse.body()!!.data!!
                        tvPetName.text = pet.name
                        tvPetBreed.text = pet.breed
                        tvPetAge.text = pet.age
                        tvPetType.text = pet.type
                        tvPetLocation.text = pet.location
                        if (!pet.imageUrl.isNullOrEmpty()) {
                            val fullUrl = if (pet.imageUrl.startsWith("http")) pet.imageUrl
                            else "https://net-vanquish-poise.ngrok-free.dev${pet.imageUrl}"
                            Glide.with(this@ViewRequestsActivity).load(fullUrl).centerCrop().into(ivPetImage)
                        }
                    }
                    if (requestResponse.isSuccessful && requestResponse.body()?.success == true) {
                        val requests = requestResponse.body()?.data ?: listOf()
                        if (requests.isEmpty()) {
                            tvEmpty.visibility = View.VISIBLE
                        } else {
                            tvEmpty.visibility = View.GONE
                            buildRequestCards(requests)
                        }
                    } else {
                        tvEmpty.text = "Failed to load requests."
                        tvEmpty.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvEmpty.text = "Connection error."
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showDeclineDialog(requestId: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_decline_reason, null)
        val etReason = dialogView.findViewById<EditText>(R.id.etDeclineReason)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Decline Request")
            .setView(dialogView)
            .setPositiveButton("Confirm") { _, _ ->
                val reason = etReason.text.toString().trim()
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Please enter a reason.", Toast.LENGTH_SHORT).show()
                } else {
                    handleDeclineWithReason(requestId, reason)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun handleDeclineWithReason(requestId: Int, reason: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.declineRequest(
                    token, requestId, mapOf("declineReason" to reason)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ViewRequestsActivity, "Request declined.", Toast.LENGTH_SHORT).show()
                        loadPetAndRequests()
                    } else {
                        Toast.makeText(this@ViewRequestsActivity, "Action failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewRequestsActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun buildRequestCards(requests: List<OwnerAdoptionRequest>) {
        llRequests.removeAllViews()
        requests.forEach { req ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_request_card, llRequests, false)

            val tvAvatar = card.findViewById<TextView>(R.id.tvAvatar)
            val tvAdopterName = card.findViewById<TextView>(R.id.tvAdopterName)
            val tvDate = card.findViewById<TextView>(R.id.tvDate)
            val tvContact = card.findViewById<TextView>(R.id.tvContact)
            val tvReason = card.findViewById<TextView>(R.id.tvReason)
            val tvNoteLabel = card.findViewById<TextView>(R.id.tvNoteLabel)
            val tvNote = card.findViewById<TextView>(R.id.tvNote)
            val llNote = card.findViewById<LinearLayout>(R.id.llNote)
            val tvStatus = card.findViewById<TextView>(R.id.tvStatus)
            val llActions = card.findViewById<LinearLayout>(R.id.llActions)
            val tvDeclineReason = card.findViewById<TextView>(R.id.tvDeclineReason)
            val btnApprove = card.findViewById<Button>(R.id.btnApprove)
            val btnDecline = card.findViewById<Button>(R.id.btnDecline)
            val btnReportAdopter = card.findViewById<Button>(R.id.btnReportAdopter)

            val initials = req.adopterName.split(" ")
                .take(2).joinToString("") { it.first().uppercase() }
            tvAvatar.text = initials
            tvAdopterName.text = req.adopterName
            tvDate.text = "Submitted ${formatDate(req.createdAt)}"
            tvContact.text = req.contactInfo
            tvReason.text = req.reason

            if (!req.noteToOwner.isNullOrBlank()) {
                tvNoteLabel.visibility = View.VISIBLE
                llNote.visibility = View.VISIBLE
                tvNote.visibility = View.VISIBLE
                tvNote.text = "\"${req.noteToOwner}\""
            } else {
                tvNoteLabel.visibility = View.GONE
                llNote.visibility = View.GONE
                tvNote.visibility = View.GONE
            }

            tvStatus.text = req.status
            when (req.status.uppercase()) {
                "APPROVED" -> {
                    tvStatus.setBackgroundResource(R.drawable.badge_available)
                    tvStatus.setTextColor(getColor(R.color.white))
                    llActions.visibility = View.GONE
                    tvDeclineReason.visibility = View.GONE
                }
                "DECLINED" -> {
                    tvStatus.setBackgroundResource(R.drawable.badge_declined)
                    tvStatus.setTextColor(getColor(R.color.white))
                    llActions.visibility = View.GONE
                    if (!req.declineReason.isNullOrBlank()) {
                        tvDeclineReason.visibility = View.VISIBLE
                        tvDeclineReason.text = "Reason: ${req.declineReason}"
                    } else {
                        tvDeclineReason.visibility = View.GONE
                    }
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.badge_pending)
                    tvStatus.setTextColor(getColor(R.color.white))
                    llActions.visibility = View.VISIBLE
                    tvDeclineReason.visibility = View.GONE
                }
            }

            btnApprove.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.instance.approveRequest(token, req.id)
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ViewRequestsActivity, "Request approved!", Toast.LENGTH_SHORT).show()
                                loadPetAndRequests()
                            } else {
                                Toast.makeText(this@ViewRequestsActivity, "Action failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ViewRequestsActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            btnDecline.setOnClickListener { showDeclineDialog(req.id) }
            btnReportAdopter?.setOnClickListener {
                val adopterId = req.adopter?.id ?: 0
                if (adopterId == 0) {
                    Toast.makeText(this, "Cannot identify adopter.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                showReportDialog(adopterId, req)
            }

            llRequests.addView(card)
        }
    }


    private fun showReportDialog(reportedUserId: Int, req: OwnerAdoptionRequest) {
        val reasons = listOf(
            "Suspicious or fake adopter",
            "Inappropriate messages",
            "No-show after approval",
            "Suspected animal abuse concern",
            "Others"
        )
        val selectedReasons = mutableSetOf<String>()

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }
        val scrollView = android.widget.ScrollView(this)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        // Show adoption request context card
        val contextCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.chip_inactive)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 24 }
        }
        contextCard.addView(TextView(this).apply {
            text = "Adoption Request Context"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(getColor(R.color.muted))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 8 }
        })
        contextCard.addView(TextView(this).apply {
            text = "👤  ${req.adopterName}"
            textSize = 13f
            setTextColor(getColor(R.color.dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 4 }
        })
        contextCard.addView(TextView(this).apply {
            text = req.contactInfo
            textSize = 13f
            setTextColor(getColor(R.color.dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 4 }
        })
        contextCard.addView(TextView(this).apply {
            text = req.reason
            textSize = 13f
            setTextColor(getColor(R.color.dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 4 }
        })
        if (!req.noteToOwner.isNullOrBlank()) {
            contextCard.addView(TextView(this).apply {
                text = req.noteToOwner ?: ""
                textSize = 13f
                setTextColor(getColor(R.color.dark))
            })
        }
        container.addView(contextCard)

        // Reason chips
        val customInput = android.widget.EditText(this).apply {
            hint = "Describe your reason..."
            setPadding(24, 16, 24, 16)
            visibility = View.GONE
            setBackgroundResource(R.drawable.chip_inactive)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = 8 }
        }
        reasons.forEach { reason ->
            val chip = TextView(this).apply {
                text = reason
                setPadding(32, 20, 32, 20)
                textSize = 14f
                setBackgroundResource(R.drawable.chip_inactive)
                setTextColor(getColor(R.color.dark))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.setMargins(0, 8, 0, 8) }
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
                if (reason == "Others")
                    customInput.visibility =
                        if (selectedReasons.contains("Others")) View.VISIBLE else View.GONE
            }
            container.addView(chip)
        }
        container.addView(customInput)
        scrollView.addView(container)
        dialogView.addView(scrollView)

        android.app.AlertDialog.Builder(this)
            .setTitle("Report ${req.adopterName}")
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
                submitReport(reportedUserId, finalReasons.joinToString("; "), req.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitReport(reportedUserId: Int, reason: String, adoptionRequestId: Int? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = ReportRequest(
                    reportedUserId = reportedUserId,
                    reason = reason,
                    adoptionRequestId = adoptionRequestId
                )
                val response = RetrofitClient.instance.submitReport(token, body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ViewRequestsActivity, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ViewRequestsActivity, "Failed to submit report.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewRequestsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val parts = dateStr.substring(0, 10).split("-")
            val months = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            "${months[parts[1].toInt()]} ${parts[2]}, ${parts[0]}"
        } catch (e: Exception) { dateStr }
    }
}