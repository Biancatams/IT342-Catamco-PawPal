package com.mobile.pawpal.features.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.Pet
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportDetailsActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvReason: TextView
    private lateinit var tvReporterName: TextView
    private lateinit var tvReporterEmail: TextView
    private lateinit var ivReporterAvatar: ImageView
    private lateinit var tvReportedName: TextView
    private lateinit var tvReportedEmail: TextView
    private lateinit var ivReportedAvatar: ImageView
    private lateinit var tvBannedBadge: TextView
    private lateinit var btnBanUser: Button
    private lateinit var btnIgnore: Button
    private lateinit var btnResolve: Button
    private lateinit var llPetCards: LinearLayout
    private lateinit var tvNoPets: TextView
    private lateinit var progressBarPets: ProgressBar
    private lateinit var btnBack: TextView
    private lateinit var cardAdoptionRequest: LinearLayout
    private lateinit var tvArAdopterName: TextView
    private lateinit var tvArContact: TextView
    private lateinit var tvArReason: TextView
    private lateinit var tvArNote: TextView

    private var reportId = 0L
    private var reportedUserId = 0L
    private var token = ""
    private val baseUrl = "https://net-vanquish-poise.ngrok-free.dev"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)

        reportId       = intent.getLongExtra("reportId", 0L)
        reportedUserId = intent.getLongExtra("reportedUserId", 0L)
        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "") ?: ""}"

        tvStatus        = findViewById(R.id.tvStatus)
        tvDate          = findViewById(R.id.tvDate)
        tvReason        = findViewById(R.id.tvReason)
        tvReporterName  = findViewById(R.id.tvReporterName)
        tvReporterEmail = findViewById(R.id.tvReporterEmail)
        ivReporterAvatar= findViewById(R.id.ivReporterAvatar)
        tvReportedName  = findViewById(R.id.tvReportedName)
        tvReportedEmail = findViewById(R.id.tvReportedEmail)
        ivReportedAvatar= findViewById(R.id.ivReportedAvatar)
        tvBannedBadge   = findViewById(R.id.tvBannedBadge)
        btnBanUser      = findViewById(R.id.btnBanUser)
        btnIgnore       = findViewById(R.id.btnIgnore)
        btnResolve      = findViewById(R.id.btnResolve)
        llPetCards      = findViewById(R.id.llPetCards)
        tvNoPets        = findViewById(R.id.tvNoPets)
        progressBarPets = findViewById(R.id.progressBarPets)
        btnBack         = findViewById(R.id.btnBack)
        cardAdoptionRequest = findViewById(R.id.cardAdoptionRequest)
        tvArAdopterName     = findViewById(R.id.tvArAdopterName)
        tvArContact         = findViewById(R.id.tvArContact)
        tvArReason          = findViewById(R.id.tvArReason)
        tvArNote            = findViewById(R.id.tvArNote)

        btnBack.setOnClickListener { finish() }

        val reporterRole = intent.getStringExtra("reporterRole") ?: ""
        val hasAr = intent.getBooleanExtra("hasAdoptionRequest", false)

        if (reporterRole.uppercase() == "PET_OWNER" && hasAr) {
            cardAdoptionRequest.visibility = View.VISIBLE
            tvArAdopterName.text = intent.getStringExtra("arAdopterName") ?: "—"
            tvArContact.text     = intent.getStringExtra("arContact") ?: "—"
            tvArReason.text      = intent.getStringExtra("arReason") ?: "—"
            val note = intent.getStringExtra("arNote")
            if (!note.isNullOrBlank()) {
                findViewById<View>(R.id.dividerArNote).visibility = View.VISIBLE
                findViewById<LinearLayout>(R.id.rowArNote).visibility = View.VISIBLE
                tvArNote.text = note
            }
        }

        if (reporterRole.uppercase() == "PET_OWNER") {
            llPetCards.visibility = View.GONE
            tvNoPets.visibility = View.GONE
            progressBarPets.visibility = View.GONE
        }

        val status = intent.getStringExtra("status") ?: "PENDING"
        tvStatus.text = status
        tvStatus.setBackgroundResource(when (status.uppercase()) {
            "RESOLVED" -> R.drawable.badge_available
            else       -> R.drawable.badge_pending
        })

        tvDate.text          = intent.getStringExtra("createdAt")?.take(10) ?: "—"
        tvReason.text        = intent.getStringExtra("reason") ?: "—"
        tvReporterName.text  = intent.getStringExtra("reporterName") ?: "—"
        tvReporterEmail.text = intent.getStringExtra("reporterEmail") ?: "—"
        tvReportedName.text  = intent.getStringExtra("reportedUserName") ?: "—"
        tvReportedEmail.text = intent.getStringExtra("reportedUserEmail") ?: "—"

        val alreadyBanned = intent.getBooleanExtra("reportedUserBanned", false)
        if (alreadyBanned) {
            tvBannedBadge.visibility = View.VISIBLE
            btnBanUser.isEnabled = false
            btnBanUser.alpha = 0.5f
            btnBanUser.text = "User Already Banned"
        }

        if (status.uppercase() == "RESOLVED") {
            disableActionButtons()
        }

        loadUserAvatar(reportedUserId.toInt(), ivReportedAvatar)
        val reporterEmail = intent.getStringExtra("reporterEmail") ?: ""
        loadReporterAvatar(reporterEmail, ivReporterAvatar)

        val cardPetListings = findViewById<LinearLayout>(R.id.cardPetListings)

        if (reporterRole.uppercase() != "PET_OWNER") {
            cardPetListings.visibility = View.VISIBLE
            loadUserPets(reportedUserId.toInt())
        } else {
            cardPetListings.visibility = View.GONE
        }

        btnBanUser.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Ban User")
                .setMessage("Ban ${intent.getStringExtra("reportedUserName")}? They will no longer be able to log in.")
                .setPositiveButton("Ban") { _, _ -> banUser() }
                .setNegativeButton("Cancel", null)
                .show()
        }
        btnIgnore.visibility = View.GONE
        btnResolve.setOnClickListener { updateStatus("RESOLVED") }
    }

    private fun loadUserAvatar(userId: Int, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllUsers(token)
                withContext(Dispatchers.Main) {
                    val user = response.body()?.data?.find { it.id == userId }
                    val url = user?.profileImageUrl
                    if (!url.isNullOrEmpty()) {
                        val fullUrl = if (url.startsWith("http")) url else "$baseUrl$url"
                        Glide.with(this@ReportDetailsActivity).load(fullUrl).circleCrop().into(imageView)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadReporterAvatar(email: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllUsers(token)
                withContext(Dispatchers.Main) {
                    val user = response.body()?.data?.find { it.email == email }
                    val url = user?.profileImageUrl
                    if (!url.isNullOrEmpty()) {
                        val fullUrl = if (url.startsWith("http")) url else "$baseUrl$url"
                        Glide.with(this@ReportDetailsActivity).load(fullUrl).circleCrop().into(imageView)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadUserPets(userId: Int) {
        progressBarPets.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllPets(token)
                withContext(Dispatchers.Main) {
                    progressBarPets.visibility = View.GONE
                    val allPets = response.body()?.data?.pets ?: emptyList()
                    val userPets = allPets.filter { it.owner?.id == userId }
                    if (userPets.isEmpty()) {
                        tvNoPets.visibility = View.VISIBLE
                    } else {
                        buildPetCards(userPets)
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    progressBarPets.visibility = View.GONE
                    tvNoPets.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun buildPetCards(pets: List<Pet>) {
        llPetCards.removeAllViews()
        if (pets.isEmpty()) { tvNoPets.visibility = View.VISIBLE; return }
        tvNoPets.visibility = View.GONE
        pets.forEach { pet ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundResource(R.drawable.chip_inactive)
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 12 }
            }

            // Pet image
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(140, 140).also { it.marginEnd = 16 }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundResource(R.drawable.chip_inactive)
            }
            if (!pet.imageUrl.isNullOrEmpty()) {
                val url = if (pet.imageUrl.startsWith("http")) pet.imageUrl else "$baseUrl${pet.imageUrl}"
                Glide.with(this).load(url).centerCrop().into(iv)
            } else {
                iv.setImageResource(R.drawable.pawlogo2)
            }
            card.addView(iv)

            // Pet info
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            info.addView(TextView(this).apply {
                text = pet.name
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.dark))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 4 }
            })
            info.addView(TextView(this).apply {
                text = "${pet.type ?: ""} · ${pet.breed}"
                textSize = 12f
                setTextColor(getColor(R.color.muted))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 4 }
            })
            info.addView(TextView(this).apply {
                text = "📍 ${pet.location}"
                textSize = 12f
                setTextColor(getColor(R.color.muted))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 6 }
            })

            // Status badge
            val statusBadge = TextView(this).apply {
                text = pet.status.replace("_", " ")
                textSize = 10f
                setTextColor(getColor(R.color.white))
                setBackgroundResource(when (pet.status.uppercase()) {
                    "AVAILABLE"    -> R.drawable.badge_available
                    "UNDER_REVIEW" -> R.drawable.badge_pending
                    else           -> R.drawable.badge_declined
                })
                setPadding(16, 6, 16, 6)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            info.addView(statusBadge)
            card.addView(info)

            // Tap to show full pet detail dialog
            card.setOnClickListener { showPetDetailDialog(pet) }
            llPetCards.addView(card)
        }
    }

    private fun showPetDetailDialog(pet: Pet) {
        progressBarPets.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getPetById(token, pet.id)
                withContext(Dispatchers.Main) {
                    progressBarPets.visibility = View.GONE
                    val detail = response.body()?.data ?: return@withContext
                    val dialogView = LinearLayout(this@ReportDetailsActivity).apply {
                        orientation = LinearLayout.VERTICAL; setPadding(48, 48, 48, 48)
                    }

                    val imageView = ImageView(this@ReportDetailsActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 600
                        ).also { it.bottomMargin = 24 }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setBackgroundResource(R.drawable.chip_inactive)
                    }
                    if (!detail.imageUrl.isNullOrEmpty()) {
                        val url = if (detail.imageUrl.startsWith("http")) detail.imageUrl else "$baseUrl${detail.imageUrl}"
                        Glide.with(this@ReportDetailsActivity).load(url).centerCrop().into(imageView)
                    }
                    dialogView.addView(imageView)

                    val nameRow = LinearLayout(this@ReportDetailsActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).also { it.bottomMargin = 16 }
                    }
                    nameRow.addView(TextView(this@ReportDetailsActivity).apply {
                        text = detail.name; textSize = 20f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(getColor(R.color.dark))
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    nameRow.addView(TextView(this@ReportDetailsActivity).apply {
                        text = pet.status.replace("_", " "); textSize = 11f
                        setTextColor(getColor(R.color.white))
                        setBackgroundResource(when (pet.status.uppercase()) {
                            "UNDER_REVIEW" -> R.drawable.badge_pending
                            "AVAILABLE"    -> R.drawable.badge_available
                            else           -> R.drawable.badge_declined
                        })
                        setPadding(20, 8, 20, 8)
                    })
                    dialogView.addView(nameRow)

                    fun detailRow(icon: String, value: String?) =
                        TextView(this@ReportDetailsActivity).apply {
                            text = "$icon  ${value ?: "—"}"; textSize = 13f
                            setTextColor(getColor(R.color.dark))
                            setPadding(20, 14, 20, 14)
                            setBackgroundResource(R.drawable.chip_inactive)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).also { it.bottomMargin = 10 }
                        }

                    dialogView.addView(detailRow("🐾", "${detail.type} · ${detail.breed}"))
                    dialogView.addView(detailRow("🎂", detail.age))
                    dialogView.addView(detailRow("⚥", detail.gender ?: "Not specified"))
                    dialogView.addView(detailRow("📍", detail.location))
                    if (!detail.description.isNullOrEmpty())
                        dialogView.addView(detailRow("📝", detail.description))

                    val healthRow = LinearLayout(this@ReportDetailsActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).also { it.bottomMargin = 12 }
                    }
                    fun healthChip(label: String, active: Boolean) =
                        TextView(this@ReportDetailsActivity).apply {
                            text = if (active) "✓ $label" else "✕ $label"; textSize = 11f
                            setTextColor(if (active) getColor(R.color.white) else getColor(R.color.muted))
                            setBackgroundResource(if (active) R.drawable.chip_active else R.drawable.chip_inactive)
                            setPadding(16, 8, 16, 8)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).also { it.marginEnd = 8 }
                        }
                    healthRow.addView(healthChip("Vaccinated", detail.vaccinated))
                    healthRow.addView(healthChip("Neutered", detail.neutered))
                    healthRow.addView(healthChip("Microchipped", detail.microchipped))
                    dialogView.addView(healthRow)

                    if (!detail.characteristics.isNullOrEmpty()) {
                        val charRow = LinearLayout(this@ReportDetailsActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).also { it.bottomMargin = 12 }
                        }
                        detail.characteristics.forEach { trait ->
                            charRow.addView(TextView(this@ReportDetailsActivity).apply {
                                text = trait; textSize = 11f
                                setTextColor(getColor(R.color.dark))
                                setBackgroundResource(R.drawable.chip_inactive)
                                setPadding(16, 8, 16, 8)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).also { it.marginEnd = 8 }
                            })
                        }
                        dialogView.addView(charRow)
                    }

                    val scrollView = android.widget.ScrollView(this@ReportDetailsActivity).apply {
                        addView(dialogView)
                    }
                    AlertDialog.Builder(this@ReportDetailsActivity)
                        .setView(scrollView)
                        .setPositiveButton("Close", null)
                        .show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBarPets.visibility = View.GONE
                    Toast.makeText(this@ReportDetailsActivity, "Failed to load pet details.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun banUser() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = RetrofitClient.instance.banUser(token, reportedUserId.toInt())
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@ReportDetailsActivity, "User banned.", Toast.LENGTH_SHORT).show()
                        btnBanUser.isEnabled = false
                        btnBanUser.alpha = 0.5f
                        btnBanUser.text = "User Already Banned"
                        tvBannedBadge.visibility = View.VISIBLE
                        updateStatus("RESOLVED")
                    } else {
                        Toast.makeText(this@ReportDetailsActivity, "Failed to ban user.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateStatus(newStatus: String) {
        android.util.Log.d("REPORT_DEBUG", "Calling updateStatus: reportId=$reportId token=$token newStatus=$newStatus")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = RetrofitClient.instance.updateReportStatus(token, reportId, mapOf("status" to newStatus))
                android.util.Log.d("REPORT_DEBUG", "Response code: ${res.code()}")
                android.util.Log.d("REPORT_DEBUG", "Error body: ${res.errorBody()?.string()}")
                android.util.Log.d("REPORT_DEBUG", "Is successful: ${res.isSuccessful}")
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        Toast.makeText(this@ReportDetailsActivity, "Report marked as $newStatus.", Toast.LENGTH_SHORT).show()
                        tvStatus.text = newStatus
                        tvStatus.setBackgroundResource(when (newStatus.uppercase()) {
                            "RESOLVED" -> R.drawable.badge_available
                            else       -> R.drawable.badge_pending
                        })
                        disableActionButtons()
                        finish()
                    } else {
                        Toast.makeText(this@ReportDetailsActivity, "Failed: ${res.code()} - ${res.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("REPORT_DEBUG", "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportDetailsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun disableActionButtons() {
        listOf(btnBanUser, btnIgnore, btnResolve).forEach {
            it.isEnabled = false; it.alpha = 0.5f
        }
    }
}