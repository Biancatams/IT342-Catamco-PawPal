package com.mobile.pawpal.features.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.features.auth.LandingActivity
import com.mobile.pawpal.shared.AdminUserItem
import com.mobile.pawpal.shared.Pet
import com.mobile.pawpal.shared.ReportItem
import com.mobile.pawpal.shared.RetrofitClient
import com.mobile.pawpal.shared.VerificationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvStatPending: TextView
    private lateinit var tvStatApproved: TextView
    private lateinit var tvStatRejected: TextView
    private lateinit var tvStatTotal: TextView
    private lateinit var cardPending: LinearLayout
    private lateinit var cardApproved: LinearLayout
    private lateinit var cardRejected: LinearLayout
    private lateinit var cardTotal: LinearLayout
    private lateinit var tabPending: TextView
    private lateinit var tabApproved: TextView
    private lateinit var tabRejected: TextView
    private lateinit var tabAll: TextView
    private lateinit var llPetList: LinearLayout
    private lateinit var llUserList: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var btnLogout: Button
    private lateinit var llVerificationList: LinearLayout

    private lateinit var navPets: FrameLayout
    private lateinit var navHome: FrameLayout
    private lateinit var navVerification: FrameLayout
    private lateinit var navIconPets: ImageView
    private lateinit var navIconHome: ImageView
    private lateinit var navIconVerif: ImageView
    private lateinit var navLabelPets: TextView
    private lateinit var navLabelHome: TextView
    private lateinit var navLabelVerif: TextView
    private lateinit var navBadgePets: TextView
    private lateinit var navBadgeHome: TextView
    private lateinit var navBadgeVerif: TextView

    private lateinit var navReports: FrameLayout
    private lateinit var navIconReports: ImageView
    private lateinit var navLabelReports: TextView
    private lateinit var navBadgeReports: TextView
    private lateinit var llReportsList: LinearLayout
    private var allReports = listOf<ReportItem>()

    private var currentSection = "HOME"
    private var token = ""
    private var allPets = listOf<Pet>()
    private var currentFilter = "UNDER_REVIEW"
    private var verifRoleFilter = "ALL_ROLES"
    private var verifStatusFilter = "PENDING"
    private var allVerifications = listOf<VerificationData>()
    private var allUsers = listOf<AdminUserItem>()
    private var userRoleFilter = "ALL"
    private var reportStatusFilter = "ALL"

    private lateinit var tvStatPendingLabel: TextView
    private lateinit var tvStatApprovedLabel: TextView
    private lateinit var tvStatRejectedLabel: TextView
    private lateinit var tvStatTotalLabel: TextView

    private val petRejectReasons = listOf(
        "Inappropriate or offensive content",
        "Fake or misleading listing",
        "Poor quality or unclear photo",
        "Incomplete or missing information",
        "Suspected animal abuse or neglect",
        "Duplicate listing",
        "Others..."
    )

    private val verifRejectReasons = listOf(
        "ID image is blurry or unreadable",
        "ID appears to be expired",
        "ID does not match provided name",
        "Invalid or unacceptable ID type",
        "Incomplete submission",
        "Suspected fake or tampered ID",
        "Others..."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_admin_dashboard)

        tvStatPending = findViewById(R.id.tvStatPending)
        tvStatApproved = findViewById(R.id.tvStatApproved)
        tvStatRejected = findViewById(R.id.tvStatRejected)
        tvStatTotal = findViewById(R.id.tvStatTotal)
        cardPending = findViewById(R.id.cardPending)
        cardApproved = findViewById(R.id.cardApproved)
        cardRejected = findViewById(R.id.cardRejected)
        cardTotal = findViewById(R.id.cardTotal)
        tabPending = findViewById(R.id.tabPending)
        tabApproved = findViewById(R.id.tabApproved)
        tabRejected = findViewById(R.id.tabRejected)
        tabAll = findViewById(R.id.tabAll)
        llPetList = findViewById(R.id.llPetList)
        llUserList = findViewById(R.id.llUserList)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnLogout = findViewById(R.id.btnLogout)
        llVerificationList = findViewById(R.id.llVerificationList)
        navPets = findViewById(R.id.navPets)
        navHome = findViewById(R.id.navHome)
        navVerification = findViewById(R.id.navVerification)
        navIconPets = findViewById(R.id.navIconPets)
        navIconHome = findViewById(R.id.navIconHome)
        navIconVerif = findViewById(R.id.navIconVerif)
        navLabelPets = findViewById(R.id.navLabelPets)
        navLabelHome = findViewById(R.id.navLabelHome)
        navLabelVerif = findViewById(R.id.navLabelVerif)
        navBadgePets = findViewById(R.id.navBadgePets)
        navBadgeHome = findViewById(R.id.navBadgeHome)
        navBadgeVerif = findViewById(R.id.navBadgeVerif)
        tvStatPendingLabel = findViewById(R.id.tvStatPendingLabel)
        tvStatApprovedLabel = findViewById(R.id.tvStatApprovedLabel)
        tvStatRejectedLabel = findViewById(R.id.tvStatRejectedLabel)
        tvStatTotalLabel = findViewById(R.id.tvStatTotalLabel)

        navReports = findViewById(R.id.navReports)
        navIconReports = findViewById(R.id.navIconReports)
        navLabelReports = findViewById(R.id.navLabelReports)
        navBadgeReports = findViewById(R.id.navBadgeReports)
        llReportsList = findViewById(R.id.llReportsList)


        btnLogout.setOnClickListener {
            getSharedPreferences("pawpal_prefs", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, LandingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        navPets.setOnClickListener { switchToPetsSection() }
        navHome.setOnClickListener { switchToHomeSection() }
        navVerification.setOnClickListener { switchToVerificationSection() }
        navReports.setOnClickListener { switchToReportsSection() }
    }

    private fun setNavActive(section: String) {
        val color = getColor(R.color.green)
        val muted = getColor(R.color.muted)
        navLabelPets.setTextColor(muted); navIconPets.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelHome.setTextColor(muted); navIconHome.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelVerif.setTextColor(muted); navIconVerif.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelReports.setTextColor(muted); navIconReports.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        when (section) {
            "PETS" -> { navLabelPets.setTextColor(color); navIconPets.setColorFilter(color) }
            "HOME" -> { navLabelHome.setTextColor(color); navIconHome.setColorFilter(color) }
            "VERIFICATION" -> { navLabelVerif.setTextColor(color); navIconVerif.setColorFilter(color) }
            "REPORTS" -> { navLabelReports.setTextColor(color); navIconReports.setColorFilter(color) }
        }
        llPetList.visibility = if (section == "PETS") View.VISIBLE else View.GONE
        llUserList.visibility = if (section == "HOME") View.VISIBLE else View.GONE
        llVerificationList.visibility = if (section == "VERIFICATION") View.VISIBLE else View.GONE
        llReportsList.visibility = if (section == "REPORTS") View.VISIBLE else View.GONE
    }

    private fun hideFilterTabs() {
        listOf(tabPending, tabApproved, tabRejected, tabAll).forEach {
            it.visibility = View.GONE
            it.setBackgroundResource(R.drawable.chip_inactive)
            it.setTextColor(getColor(R.color.dark))
        }
    }

    private fun showFilterTabs() {
        listOf(tabPending, tabApproved, tabRejected, tabAll).forEach {
            it.visibility = View.VISIBLE
            it.setBackgroundResource(R.drawable.chip_inactive)
            it.setTextColor(getColor(R.color.dark))
        }
    }

    private fun switchToPetsSection() {
        currentSection = "PETS"
        setNavActive("PETS")
        showFilterTabs()
        tabRejected.visibility = View.VISIBLE
        setPetTabListeners()
        updateStats()
        setFilter(currentFilter)
    }

    private fun switchToHomeSection() {
        currentSection = "HOME"
        setNavActive("HOME")
        hideFilterTabs()
        updateUserStats()
        loadUsers()
    }

    private fun switchToVerificationSection() {
        currentSection = "VERIFICATION"
        setNavActive("VERIFICATION")
        showFilterTabs()
        tabRejected.visibility = View.VISIBLE
        verifStatusFilter = "PENDING"
        verifRoleFilter = "ALL_ROLES"
        setVerifTabListeners()
        loadVerifications()
    }

    private fun setVerifTabListeners() {
        listOf(tabAll, tabPending, tabApproved, tabRejected).forEach {
            it.setBackgroundResource(R.drawable.chip_inactive)
            it.setTextColor(getColor(R.color.dark))
        }
        tabPending.setBackgroundResource(R.drawable.chip_active)
        tabPending.setTextColor(getColor(R.color.white))
        tabAll.setOnClickListener { verifStatusFilter = "ALL"; applyVerifFilter() }
        tabPending.setOnClickListener { verifStatusFilter = "PENDING"; applyVerifFilter() }
        tabApproved.setOnClickListener { verifStatusFilter = "APPROVED"; applyVerifFilter() }
        tabRejected.setOnClickListener { verifStatusFilter = "REJECTED"; applyVerifFilter() }
    }


    private fun setPetTabListeners() {
        listOf(tabAll, tabPending, tabApproved, tabRejected).forEach { it.setBackgroundResource(R.drawable.chip_inactive); it.setTextColor(getColor(R.color.dark)) }
        tabPending.setBackgroundResource(R.drawable.chip_active); tabPending.setTextColor(getColor(R.color.white))
        tabPending.setOnClickListener { setFilter("UNDER_REVIEW") }
        tabApproved.setOnClickListener { setFilter("AVAILABLE") }
        tabRejected.setOnClickListener { setFilter("REJECTED") }
        tabAll.setOnClickListener { setFilter("ALL") }
    }

    override fun onResume() {
        super.onResume()
        token = "Bearer ${getSharedPreferences("pawpal_prefs", MODE_PRIVATE).getString("token", "")}"
        loadUsers()
        loadAllPets()
        loadVerifications()
        loadReports()
    }

    private fun updateNavBadges() {
        val petPending = allPets.count { it.status.uppercase() == "UNDER_REVIEW" }
        val verifPending = allVerifications.count { it.status.uppercase() == "PENDING" }
        val reportsPending = allReports.count { it.status.uppercase() == "PENDING" }
        navBadgePets.visibility = if (petPending > 0) View.VISIBLE else View.GONE
        navBadgePets.text = if (petPending > 99) "99+" else petPending.toString()
        navBadgeVerif.visibility = if (verifPending > 0) View.VISIBLE else View.GONE
        navBadgeVerif.text = if (verifPending > 99) "99+" else verifPending.toString()
        navBadgeReports.visibility = if (reportsPending > 0) View.VISIBLE else View.GONE
        navBadgeReports.text = if (reportsPending > 99) "99+" else reportsPending.toString()
    }

    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllUsers(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    allUsers = response.body()?.data ?: emptyList()
                    updateUserStats()
                    buildUserList(allUsers)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE; Toast.makeText(this@AdminDashboardActivity, "Connection error.", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun updateUserStats() {
        val adopters = allUsers.count { it.role.uppercase() == "ADOPTER" }
        val owners = allUsers.count { it.role.uppercase() == "PET_OWNER" }
        val banned = allUsers.count { it.isBanned }
        val total = allUsers.size
        tvStatPendingLabel.text = "Adopters"
        tvStatApprovedLabel.text = "Owners"
        tvStatRejectedLabel.text = "Banned"
        tvStatTotalLabel.text = "Total"
        tvStatPending.text = adopters.toString()
        tvStatApproved.text = owners.toString()
        tvStatRejected.text = banned.toString()
        tvStatTotal.text = total.toString()
        cardRejected.visibility = View.VISIBLE
        listOf(cardPending, cardApproved, cardRejected, cardTotal).forEach {
            it.setBackgroundResource(R.drawable.card_background)
        }
        when (userRoleFilter) {
            "ADOPTER"  -> cardPending.setBackgroundResource(R.drawable.stat_card_active)
            "PET_OWNER"-> cardApproved.setBackgroundResource(R.drawable.stat_card_active)
            "BANNED"   -> cardRejected.setBackgroundResource(R.drawable.stat_card_active)
            else       -> cardTotal.setBackgroundResource(R.drawable.stat_card_active)
        }
        cardPending.setOnClickListener {
            userRoleFilter = "ADOPTER"
            updateUserStats()
            renderUserCards(allUsers.filter { it.role.uppercase() == "ADOPTER" })
        }
        cardApproved.setOnClickListener {
            userRoleFilter = "PET_OWNER"
            updateUserStats()
            renderUserCards(allUsers.filter { it.role.uppercase() == "PET_OWNER" })
        }
        cardRejected.setOnClickListener {
            userRoleFilter = "BANNED"
            updateUserStats()
            renderUserCards(allUsers.filter { it.isBanned })
        }
        cardTotal.setOnClickListener {
            userRoleFilter = "ALL"
            updateUserStats()
            renderUserCards(allUsers)
        }
    }

    private fun buildUserList(users: List<AdminUserItem>) {
        llUserList.removeAllViews()

        val toggleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 16 }
        }
        fun makeToggle(label: String, filter: String) = TextView(this).apply {
            text = label; textSize = 13f; gravity = android.view.Gravity.CENTER
            setPadding(32, 20, 32, 20)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also { it.marginEnd = 8 }
            setBackgroundResource(if (userRoleFilter == filter) R.drawable.chip_active else R.drawable.chip_inactive)
            setTextColor(if (userRoleFilter == filter) getColor(R.color.white) else getColor(R.color.dark))
        }
        val btnAll = makeToggle("All", "ALL")
        val btnAdopter = makeToggle("Adopter", "ADOPTER")
        val btnOwner = makeToggle("Pet Owner", "PET_OWNER")
        val btnBanned = makeToggle("Banned", "BANNED")

        fun updateToggle(selected: String) {
            userRoleFilter = selected
            listOf(btnAll, btnAdopter, btnOwner, btnBanned).forEach { it.setBackgroundResource(R.drawable.chip_inactive); it.setTextColor(getColor(R.color.dark)) }
            when (selected) {
                "ALL" -> { btnAll.setBackgroundResource(R.drawable.chip_active); btnAll.setTextColor(getColor(R.color.white)) }
                "ADOPTER" -> { btnAdopter.setBackgroundResource(R.drawable.chip_active); btnAdopter.setTextColor(getColor(R.color.white)) }
                "PET_OWNER" -> { btnOwner.setBackgroundResource(R.drawable.chip_active); btnOwner.setTextColor(getColor(R.color.white)) }
                "BANNED" -> { btnBanned.setBackgroundResource(R.drawable.chip_active); btnBanned.setTextColor(getColor(R.color.white)) }
            }
            val filtered = when (selected) {
                "ADOPTER" -> allUsers.filter { it.role.uppercase() == "ADOPTER" }
                "PET_OWNER" -> allUsers.filter { it.role.uppercase() == "PET_OWNER" }
                "BANNED" -> allUsers.filter { it.isBanned }
                else -> allUsers
            }
            renderUserCards(filtered)
        }

        btnAll.setOnClickListener { updateToggle("ALL") }
        btnAdopter.setOnClickListener { updateToggle("ADOPTER") }
        btnOwner.setOnClickListener { updateToggle("PET_OWNER") }
        btnBanned.setOnClickListener { updateToggle("BANNED") }
        toggleRow.addView(btnAll); toggleRow.addView(btnAdopter); toggleRow.addView(btnOwner)
        toggleRow.addView(btnBanned)

        llUserList.addView(toggleRow)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            tag = "user_card_container"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        llUserList.addView(container)
        renderUserCards(users)
    }

    private fun renderUserCards(users: List<AdminUserItem>) {
        val container = llUserList.findViewWithTag<LinearLayout>("user_card_container") ?: return
        container.removeAllViews()
        if (users.isEmpty()) { tvEmpty.text = "No users found."; tvEmpty.visibility = View.VISIBLE; return }
        tvEmpty.visibility = View.GONE
        val baseUrl = "https://net-vanquish-poise.ngrok-free.dev"
        users.forEach { user ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_admin_user_card, container, false)
            val ivAvatar = card.findViewById<ImageView>(R.id.ivUserAvatar)
            val tvName = card.findViewById<TextView>(R.id.tvUserName)
            val tvEmail = card.findViewById<TextView>(R.id.tvUserEmail)
            val tvRole = card.findViewById<TextView>(R.id.tvUserRole)
            val tvStatus = card.findViewById<TextView>(R.id.tvUserStatus)
            val tvBanned = card.findViewById<TextView>(R.id.tvBannedBadge)
            val tvVerified = card.findViewById<TextView>(R.id.tvVerifiedBadge)

            tvName.text = user.fullName
            tvEmail.text = user.email
            tvRole.text = if (user.role == "ADOPTER") "Adopter" else "Pet Owner"
            tvRole.setBackgroundResource(if (user.role == "ADOPTER") R.drawable.badge_available else R.drawable.badge_pending)
            tvStatus.text = if (user.isVerified) "Verified" else "Unverified"
            tvStatus.setBackgroundResource(if (user.isVerified) R.drawable.badge_available else R.drawable.chip_inactive)
            if (user.isVerified) tvStatus.setTextColor(getColor(R.color.white)) else tvStatus.setTextColor(getColor(R.color.muted))
            tvBanned.visibility = if (user.isBanned) View.VISIBLE else View.GONE
            tvVerified.visibility = if (user.isVerified) View.VISIBLE else View.GONE

            if (!user.profileImageUrl.isNullOrEmpty()) {
                val url = if (user.profileImageUrl.startsWith("http")) user.profileImageUrl else "$baseUrl${user.profileImageUrl}"
                Glide.with(this).load(url).circleCrop().into(ivAvatar)
            }

            card.setOnClickListener { showUserDetailDialog(user) }
            container.addView(card)
        }
    }

    private fun showUserDetailDialog(user: AdminUserItem) {
        val baseUrl = "https://net-vanquish-poise.ngrok-free.dev"
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 48, 48, 48) }

        val profileIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).also { it.bottomMargin = 24; it.gravity = android.view.Gravity.CENTER }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundResource(R.drawable.chip_inactive)
            clipToOutline = true
        }
        if (!user.profileImageUrl.isNullOrEmpty()) {
            val url = if (user.profileImageUrl.startsWith("http")) user.profileImageUrl else "$baseUrl${user.profileImageUrl}"
            Glide.with(this).load(url).circleCrop().into(profileIv)
        }

        val profileWrapper = LinearLayout(this).apply {
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 24 }
            addView(profileIv)
        }
        dialogView.addView(profileWrapper)

        fun infoRow(label: String, value: String) = TextView(this).apply {
            text = "$label  $value"; textSize = 13f; setTextColor(getColor(R.color.dark))
            setPadding(20, 14, 20, 14)
            setBackgroundResource(R.drawable.chip_inactive)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 10 }
        }

        dialogView.addView(infoRow("👤", user.fullName))
        dialogView.addView(infoRow("✉", user.email))
        dialogView.addView(infoRow("🏷", if (user.role == "ADOPTER") "Adopter" else "Pet Owner"))
        dialogView.addView(infoRow("✆", user.phoneNumber?.takeIf { it.isNotBlank() } ?: "No phone"))
        dialogView.addView(infoRow("⌂", user.address?.takeIf { it.isNotBlank() } ?: "No address"))
        dialogView.addView(infoRow("🗓", user.createdAt?.take(10) ?: ""))
        dialogView.addView(infoRow("✓", if (user.isVerified) "Verified" else "Not verified"))

        if (!user.isBanned) {
            val btnBan = Button(this).apply {
                text = "Ban User"
                setTextColor(getColor(R.color.white))
                backgroundTintList = getColorStateList(R.color.terracotta)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = 24 }
            }
            btnBan.setOnClickListener {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Ban User")
                    .setMessage("Are you sure you want to ban ${user.fullName}? They will no longer be able to log in.")
                    .setPositiveButton("Ban") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val res = RetrofitClient.instance.banUser(token, user.id)
                                withContext(Dispatchers.Main) {
                                    if (res.isSuccessful) {
                                        Toast.makeText(this@AdminDashboardActivity, "${user.fullName} has been banned.", Toast.LENGTH_SHORT).show()
                                        loadUsers()
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                    .setNegativeButton("Cancel", null).show()
            }
            dialogView.addView(btnBan)
        }

        val scrollView = android.widget.ScrollView(this).apply { addView(dialogView) }
        android.app.AlertDialog.Builder(this).setView(scrollView).setPositiveButton("Close", null).show()
    }

    private fun loadAllPets() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allResponse = RetrofitClient.instance.getAllPets(token)
                val underReviewResponse = RetrofitClient.instance.getUnderReviewPets(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    val allFromServer = if (allResponse.isSuccessful) allResponse.body()?.data?.pets ?: listOf() else listOf()
                    val underReview = if (underReviewResponse.isSuccessful) underReviewResponse.body()?.data?.pets ?: listOf() else listOf()
                    allPets = (underReview + allFromServer).distinctBy { it.id }
                    if (currentSection == "PETS") { updateStats(); setFilter(currentFilter) }
                    updateNavBadges()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE; tvEmpty.text = "Connection error."; tvEmpty.visibility = View.VISIBLE }
            }
        }
    }

    private fun updateStats() {
        tvStatPendingLabel.text = "Pending"
        tvStatApprovedLabel.text = "Approved"
        tvStatRejectedLabel.text = "Rejected"
        tvStatTotalLabel.text = "Total"
        val pending = allPets.count { it.status.uppercase() == "UNDER_REVIEW" }
        val approved = allPets.count { it.status.uppercase() == "AVAILABLE" }
        val rejected = allPets.count { it.status.uppercase() == "REJECTED" }
        tvStatPending.text = pending.toString()
        tvStatApproved.text = approved.toString()
        tvStatRejected.text = rejected.toString()
        tvStatTotal.text = allPets.size.toString()
        tabPending.text = "Pending ($pending)"
        tabApproved.text = "Approved ($approved)"
        tabRejected.text = "Rejected ($rejected)"
        tabAll.text = "All (${allPets.size})"
    }

    private fun updateVerifStats() {
        tvStatPendingLabel.text = "Pending"
        tvStatApprovedLabel.text = "Approved"
        tvStatRejectedLabel.text = "Rejected"
        tvStatTotalLabel.text = "Total"
        val pending = allVerifications.count { it.status.uppercase() == "PENDING" }
        val approved = allVerifications.count { it.status.uppercase() == "APPROVED" }
        val rejected = allVerifications.count { it.status.uppercase() == "REJECTED" }
        tvStatPending.text = pending.toString()
        tvStatApproved.text = approved.toString()
        tvStatRejected.text = rejected.toString()
        tvStatTotal.text = allVerifications.size.toString()
        tabPending.text = "Pending ($pending)"
        tabApproved.text = "Approved ($approved)"
        tabRejected.text = "Rejected ($rejected)"
        tabAll.text = "All (${allVerifications.size})"
    }

    private fun updateReportStats() {
        val pending = allReports.count { it.status.uppercase() == "PENDING" && !it.reportedUserBanned }
        val resolved = allReports.count { it.status.uppercase() == "RESOLVED" || it.reportedUserBanned }
        val total = allReports.size
        tvStatPendingLabel.text = "Pending"
        tvStatApprovedLabel.text = "Resolved"
        tvStatRejectedLabel.text = ""
        tvStatTotalLabel.text = "Total"
        tvStatPending.text = pending.toString()
        tvStatApproved.text = resolved.toString()
        tvStatRejected.text = ""
        tvStatTotal.text = total.toString()
        // Highlight the active stat card
        listOf(cardPending, cardApproved, cardRejected, cardTotal).forEach {
            it.setBackgroundResource(R.drawable.card_background)
        }
        cardRejected.visibility = View.GONE
        when (reportStatusFilter) {
            "ALL"     -> cardTotal.setBackgroundResource(R.drawable.stat_card_active)
            "PENDING" -> cardPending.setBackgroundResource(R.drawable.stat_card_active)
            "RESOLVED"-> cardApproved.setBackgroundResource(R.drawable.stat_card_active)
        }
        cardPending.setOnClickListener {
            reportStatusFilter = "PENDING"
            updateReportStats()
            setReportTabActive("PENDING")
            buildReportsList(allReports.filter { it.status.uppercase() == "PENDING" })
        }
        cardApproved.setOnClickListener {
            reportStatusFilter = "RESOLVED"
            updateReportStats()
            setReportTabActive("RESOLVED")
            buildReportsList(allReports.filter { it.status.uppercase() == "RESOLVED" || it.reportedUserBanned })
        }
        cardTotal.setOnClickListener {
            reportStatusFilter = "ALL"
            updateReportStats()
            setReportTabActive("ALL")
            buildReportsList(allReports)
        }
    }

    private fun setReportTabActive(filter: String) {
        listOf(tabPending, tabApproved, tabAll).forEach {
            it.setBackgroundResource(R.drawable.chip_inactive)
            it.setTextColor(getColor(R.color.dark))
        }
        when (filter) {
            "ALL"      -> { tabAll.setBackgroundResource(R.drawable.chip_active); tabAll.setTextColor(getColor(R.color.white)) }
            "PENDING"  -> { tabPending.setBackgroundResource(R.drawable.chip_active); tabPending.setTextColor(getColor(R.color.white)) }
            "RESOLVED" -> { tabApproved.setBackgroundResource(R.drawable.chip_active); tabApproved.setTextColor(getColor(R.color.white)) }
        }
    }
    private fun setFilter(filter: String) {
        currentFilter = filter
        listOf(cardPending, cardApproved, cardRejected, cardTotal).forEach { it.setBackgroundResource(R.drawable.card_background) }
        listOf(tabPending, tabApproved, tabRejected, tabAll).forEach { it.setBackgroundResource(R.drawable.chip_inactive); it.setTextColor(getColor(R.color.dark)) }
        when (filter) {
            "UNDER_REVIEW" -> { cardPending.setBackgroundResource(R.drawable.stat_card_active); tabPending.setBackgroundResource(R.drawable.chip_active); tabPending.setTextColor(getColor(R.color.white)) }
            "AVAILABLE" -> { cardApproved.setBackgroundResource(R.drawable.stat_card_active); tabApproved.setBackgroundResource(R.drawable.chip_active); tabApproved.setTextColor(getColor(R.color.white)) }
            "REJECTED" -> { cardRejected.setBackgroundResource(R.drawable.stat_card_active); tabRejected.setBackgroundResource(R.drawable.chip_active); tabRejected.setTextColor(getColor(R.color.white)) }
            "ALL" -> { cardTotal.setBackgroundResource(R.drawable.stat_card_active); tabAll.setBackgroundResource(R.drawable.chip_active); tabAll.setTextColor(getColor(R.color.white)) }
        }
        buildPetList(if (filter == "ALL") allPets else allPets.filter { it.status.uppercase() == filter })
    }

    private fun applyVerifFilter() {
        listOf(tabAll, tabPending, tabApproved, tabRejected).forEach { it.setBackgroundResource(R.drawable.chip_inactive); it.setTextColor(getColor(R.color.dark)) }
        when (verifStatusFilter) {
            "ALL" -> { tabAll.setBackgroundResource(R.drawable.chip_active); tabAll.setTextColor(getColor(R.color.white)) }
            "PENDING" -> { tabPending.setBackgroundResource(R.drawable.chip_active); tabPending.setTextColor(getColor(R.color.white)) }
            "APPROVED" -> { tabApproved.setBackgroundResource(R.drawable.chip_active); tabApproved.setTextColor(getColor(R.color.white)) }
            "REJECTED" -> { tabRejected.setBackgroundResource(R.drawable.chip_active); tabRejected.setTextColor(getColor(R.color.white)) }
        }
        var filtered = when (verifStatusFilter) {
            "PENDING" -> allVerifications.filter { it.status.uppercase() == "PENDING" }
            "APPROVED" -> allVerifications.filter { it.status.uppercase() == "APPROVED" }
            "REJECTED" -> allVerifications.filter { it.status.uppercase() == "REJECTED" }
            else -> allVerifications.sortedBy { mapOf("PENDING" to 0, "APPROVED" to 1, "REJECTED" to 2)[it.status.uppercase()] ?: 3 }
        }
        if (verifRoleFilter != "ALL_ROLES") filtered = filtered.filter { it.user?.role?.uppercase() == verifRoleFilter }
        buildVerificationList(filtered)
    }

    private fun buildPetList(pets: List<Pet>) {
        llPetList.removeAllViews()
        if (pets.isEmpty()) { tvEmpty.visibility = View.VISIBLE; return }
        tvEmpty.visibility = View.GONE
        val baseUrl = "https://net-vanquish-poise.ngrok-free.dev"
        pets.forEach { pet ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_admin_pet_card, llPetList, false)
            val iv = card.findViewById<ImageView>(R.id.ivPetImage)
            val tvName = card.findViewById<TextView>(R.id.tvPetName)
            val tvMeta = card.findViewById<TextView>(R.id.tvMeta)
            val tvOwner = card.findViewById<TextView>(R.id.tvOwner)
            val tvLocation = card.findViewById<TextView>(R.id.tvLocation)
            val tvStatus = card.findViewById<TextView>(R.id.tvStatus)
            val llActions = card.findViewById<LinearLayout>(R.id.llActions)
            val llRejection = card.findViewById<LinearLayout>(R.id.llRejection)
            val tvRejectionReason = card.findViewById<TextView>(R.id.tvRejectionReason)
            val btnApprove = card.findViewById<Button>(R.id.btnApprove)
            val btnReject = card.findViewById<Button>(R.id.btnReject)

            tvName.text = pet.name
            tvMeta.text = "${pet.type ?: ""} · ${pet.breed} · ${pet.age}"
            tvOwner.text = "👤 Owner"
            tvLocation.text = "📍 ${pet.location}"
            tvStatus.text = pet.status.replace("_", " ")

            when (pet.status.uppercase()) {
                "UNDER_REVIEW" -> { tvStatus.setBackgroundResource(R.drawable.badge_pending); tvStatus.setTextColor(getColor(R.color.white)); llActions.visibility = View.VISIBLE; llRejection.visibility = View.GONE }
                "AVAILABLE" -> { tvStatus.setBackgroundResource(R.drawable.badge_available); tvStatus.setTextColor(getColor(R.color.white)); llActions.visibility = View.GONE; llRejection.visibility = View.GONE }
                "REJECTED" -> { tvStatus.setBackgroundResource(R.drawable.badge_declined); tvStatus.setTextColor(getColor(R.color.white)); llActions.visibility = View.GONE; llRejection.visibility = View.VISIBLE; tvRejectionReason.text = "Rejection reason: \"Not suitable at this time.\"" }
                else -> { tvStatus.setBackgroundResource(R.drawable.badge_available); tvStatus.setTextColor(getColor(R.color.white)); llActions.visibility = View.GONE; llRejection.visibility = View.GONE }
            }

            if (!pet.imageUrl.isNullOrEmpty()) {
                val url = if (pet.imageUrl.startsWith("http")) pet.imageUrl else "$baseUrl${pet.imageUrl}"
                Glide.with(this).load(url).centerCrop().into(iv)
            } else {
                iv.setImageResource(R.drawable.pawlogo2)
            }

            card.setOnClickListener { showPetDetailDialog(pet) }
            btnApprove.setOnClickListener { handlePetAction(pet.id, "approve") }
            btnReject.setOnClickListener { showRejectDialog(pet.id) }
            llPetList.addView(card)
        }
    }

    private fun showPetDetailDialog(pet: Pet) {
        val baseUrl = "https://net-vanquish-poise.ngrok-free.dev"
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getPetById(token, pet.id)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    val detail = response.body()?.data ?: return@withContext
                    val dialogView = LinearLayout(this@AdminDashboardActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 48, 48, 48) }

                    val imageView = ImageView(this@AdminDashboardActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600).also { it.bottomMargin = 24 }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setBackgroundResource(R.drawable.chip_inactive)
                    }
                    if (!detail.imageUrl.isNullOrEmpty()) {
                        val url = if (detail.imageUrl.startsWith("http")) detail.imageUrl else "$baseUrl${detail.imageUrl}"
                        Glide.with(this@AdminDashboardActivity).load(url).centerCrop().into(imageView)
                    }
                    dialogView.addView(imageView)

                    val nameRow = LinearLayout(this@AdminDashboardActivity).apply {
                        orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 16 }
                    }
                    nameRow.addView(TextView(this@AdminDashboardActivity).apply {
                        text = detail.name; textSize = 20f; setTypeface(null, android.graphics.Typeface.BOLD)
                        setTextColor(getColor(R.color.dark))
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    val statusBadge = when (pet.status.uppercase()) {
                        "UNDER_REVIEW" -> R.drawable.badge_pending
                        "AVAILABLE" -> R.drawable.badge_available
                        else -> R.drawable.badge_declined
                    }
                    nameRow.addView(TextView(this@AdminDashboardActivity).apply {
                        text = pet.status.replace("_", " "); textSize = 11f; setTextColor(getColor(R.color.white))
                        setBackgroundResource(statusBadge); setPadding(20, 8, 20, 8)
                    })
                    dialogView.addView(nameRow)

                    fun detailRow(icon: String, value: String?) = TextView(this@AdminDashboardActivity).apply {
                        text = "$icon  ${value ?: "—"}"; textSize = 13f; setTextColor(getColor(R.color.dark))
                        setPadding(20, 14, 20, 14); setBackgroundResource(R.drawable.chip_inactive)
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 10 }
                    }

                    dialogView.addView(detailRow("🐾", "${detail.type} · ${detail.breed}"))
                    dialogView.addView(detailRow("🎂", detail.age))
                    dialogView.addView(detailRow("⚥", detail.gender ?: "Not specified"))
                    dialogView.addView(detailRow("📍", detail.location))
                    if (!detail.description.isNullOrEmpty()) dialogView.addView(detailRow("📝", detail.description))

                    val healthRow = LinearLayout(this@AdminDashboardActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 12 }
                    }
                    fun healthChip(label: String, active: Boolean) = TextView(this@AdminDashboardActivity).apply {
                        text = if (active) "✓ $label" else "✕ $label"; textSize = 11f
                        setTextColor(if (active) getColor(R.color.white) else getColor(R.color.muted))
                        setBackgroundResource(if (active) R.drawable.chip_active else R.drawable.chip_inactive)
                        setPadding(16, 8, 16, 8)
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.marginEnd = 8 }
                    }
                    healthRow.addView(healthChip("Vaccinated", detail.vaccinated))
                    healthRow.addView(healthChip("Neutered", detail.neutered))
                    healthRow.addView(healthChip("Microchipped", detail.microchipped))
                    dialogView.addView(healthRow)

                    if (!detail.characteristics.isNullOrEmpty()) {
                        val charRow = LinearLayout(this@AdminDashboardActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 12 }
                        }
                        detail.characteristics.forEach { trait ->
                            charRow.addView(TextView(this@AdminDashboardActivity).apply {
                                text = trait; textSize = 11f; setTextColor(getColor(R.color.dark))
                                setBackgroundResource(R.drawable.chip_inactive); setPadding(16, 8, 16, 8)
                                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.marginEnd = 8 }
                            })
                        }
                        dialogView.addView(charRow)
                    }

                    if (!detail.adminNote.isNullOrEmpty()) dialogView.addView(detailRow("🗒", detail.adminNote))

                    if (pet.status.uppercase() == "UNDER_REVIEW") {
                        val actionRow = LinearLayout(this@AdminDashboardActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = 16 }
                        }
                        val btnReject = Button(this@AdminDashboardActivity).apply {
                            text = "✕ Reject"; setTextColor(getColor(R.color.white))
                            backgroundTintList = getColorStateList(R.color.terracotta)
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also { it.marginEnd = 16 }
                            setOnClickListener { showRejectDialog(pet.id) }
                        }
                        val btnApprove = Button(this@AdminDashboardActivity).apply {
                            text = "✓ Approve"; setTextColor(getColor(R.color.white))
                            backgroundTintList = getColorStateList(R.color.green)
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            setOnClickListener { handlePetAction(pet.id, "approve") }
                        }
                        actionRow.addView(btnReject); actionRow.addView(btnApprove)
                        dialogView.addView(actionRow)
                    }

                    val scrollView = android.widget.ScrollView(this@AdminDashboardActivity).apply { addView(dialogView) }
                    android.app.AlertDialog.Builder(this@AdminDashboardActivity).setView(scrollView).setPositiveButton("Close", null).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE; Toast.makeText(this@AdminDashboardActivity, "Failed to load pet details.", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun handlePetAction(petId: Int, action: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (action == "approve") RetrofitClient.instance.approvePet(token, petId)
                else RetrofitClient.instance.rejectPet(token, petId, mapOf("reason" to "Not suitable at this time."))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) { Toast.makeText(this@AdminDashboardActivity, if (action == "approve") "Pet approved!" else "Pet rejected.", Toast.LENGTH_SHORT).show(); loadAllPets() }
                    else Toast.makeText(this@AdminDashboardActivity, "Action failed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(this@AdminDashboardActivity, "Connection error.", Toast.LENGTH_SHORT).show() } }
        }
    }

    private fun showRejectDialog(petId: Int) {
        val selectedReasons = mutableSetOf<String>()
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 32, 48, 16) }
        val scrollView = android.widget.ScrollView(this)
        val reasonsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val customInput = EditText(this).apply { hint = "Enter custom reason..."; setPadding(0, 16, 0, 0); visibility = View.GONE }
        petRejectReasons.forEach { reason ->
            val chip = TextView(this).apply {
                text = reason; setPadding(32, 20, 32, 20); textSize = 14f
                setBackgroundResource(R.drawable.chip_inactive); setTextColor(getColor(R.color.dark))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, 8, 0, 8) }
            }
            chip.setOnClickListener {
                if (selectedReasons.contains(reason)) { selectedReasons.remove(reason); chip.setBackgroundResource(R.drawable.chip_inactive); chip.setTextColor(getColor(R.color.dark)) }
                else { selectedReasons.add(reason); chip.setBackgroundResource(R.drawable.chip_active); chip.setTextColor(getColor(R.color.white)) }
                if (reason == "Others...") customInput.visibility = if (selectedReasons.contains("Others...")) View.VISIBLE else View.GONE
            }
            reasonsContainer.addView(chip)
        }
        reasonsContainer.addView(customInput); scrollView.addView(reasonsContainer); dialogView.addView(scrollView)
        android.app.AlertDialog.Builder(this).setTitle("Reject Pet Listing").setMessage("Select one or more reasons:").setView(dialogView)
            .setPositiveButton("Reject") { _, _ ->
                val reasons = selectedReasons.filter { it != "Others..." }.toMutableList()
                if (selectedReasons.contains("Others...") && customInput.text.toString().trim().isNotEmpty()) reasons.add(customInput.text.toString().trim())
                if (reasons.isEmpty()) { Toast.makeText(this, "Please select at least one reason.", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.instance.rejectPet(token, petId, mapOf("reason" to reasons.joinToString("; ")))
                        withContext(Dispatchers.Main) { if (response.isSuccessful) { Toast.makeText(this@AdminDashboardActivity, "Pet rejected.", Toast.LENGTH_SHORT).show(); loadAllPets() } }
                    } catch (_: Exception) {}
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun loadVerifications() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllVerifications(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    allVerifications = response.body()?.data ?: emptyList()
                    if (currentSection == "VERIFICATION") { updateVerifStats(); buildRoleToggle(); applyVerifFilter() }
                    updateNavBadges()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE; Toast.makeText(this@AdminDashboardActivity, "Connection error.", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun buildRoleToggle() {
        llVerificationList.removeAllViews()
        val toggleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 16 }
        }
        val btnAll = TextView(this).apply { text = "All"; textSize = 13f; gravity = android.view.Gravity.CENTER; setPadding(32, 20, 32, 20); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also { it.marginEnd = 8 }; setBackgroundResource(if (verifRoleFilter == "ALL_ROLES") R.drawable.chip_active else R.drawable.chip_inactive); setTextColor(if (verifRoleFilter == "ALL_ROLES") getColor(R.color.white) else getColor(R.color.dark)) }
        val btnAdopter = TextView(this).apply { text = "Adopter"; textSize = 13f; gravity = android.view.Gravity.CENTER; setPadding(32, 20, 32, 20); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also { it.marginEnd = 8 }; setBackgroundResource(if (verifRoleFilter == "ADOPTER") R.drawable.chip_active else R.drawable.chip_inactive); setTextColor(if (verifRoleFilter == "ADOPTER") getColor(R.color.white) else getColor(R.color.dark)) }
        val btnOwner = TextView(this).apply { text = "Pet Owner"; textSize = 13f; gravity = android.view.Gravity.CENTER; setPadding(32, 20, 32, 20); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); setBackgroundResource(if (verifRoleFilter == "PET_OWNER") R.drawable.chip_active else R.drawable.chip_inactive); setTextColor(if (verifRoleFilter == "PET_OWNER") getColor(R.color.white) else getColor(R.color.dark)) }

        fun updateToggle(selected: String) {
            verifRoleFilter = selected
            listOf(btnAll, btnAdopter, btnOwner).forEach { it.setBackgroundResource(R.drawable.chip_inactive); it.setTextColor(getColor(R.color.dark)) }
            when (selected) {
                "ALL_ROLES" -> { btnAll.setBackgroundResource(R.drawable.chip_active); btnAll.setTextColor(getColor(R.color.white)) }
                "ADOPTER" -> { btnAdopter.setBackgroundResource(R.drawable.chip_active); btnAdopter.setTextColor(getColor(R.color.white)) }
                "PET_OWNER" -> { btnOwner.setBackgroundResource(R.drawable.chip_active); btnOwner.setTextColor(getColor(R.color.white)) }
            }
            applyVerifFilter()
        }
        btnAll.setOnClickListener { updateToggle("ALL_ROLES") }
        btnAdopter.setOnClickListener { updateToggle("ADOPTER") }
        btnOwner.setOnClickListener { updateToggle("PET_OWNER") }
        toggleRow.addView(btnAll); toggleRow.addView(btnAdopter); toggleRow.addView(btnOwner)
        llVerificationList.addView(toggleRow)
        val listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; tag = "verif_list_container"; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) }
        llVerificationList.addView(listContainer)
    }

    private fun buildVerificationList(list: List<VerificationData>) {
        val listContainer = llVerificationList.findViewWithTag<LinearLayout>("verif_list_container") ?: llVerificationList
        listContainer.removeAllViews()
        if (list.isEmpty()) { tvEmpty.text = "No verification requests."; tvEmpty.visibility = View.VISIBLE; return }
        tvEmpty.visibility = View.GONE
        val baseUrl = "https://net-vanquish-poise.ngrok-free.dev"
        list.forEach { verif ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_verification_card, listContainer, false)
            val tvUserName = card.findViewById<TextView>(R.id.tvUserName)
            val tvUserEmail = card.findViewById<TextView>(R.id.tvUserEmail)
            val tvUserRole = card.findViewById<TextView>(R.id.tvUserRole)
            val tvVerifStatus = card.findViewById<TextView>(R.id.tvVerifStatus)
            val ivProfileImage = card.findViewById<ImageView>(R.id.ivProfileImage)
            val llExpandedContent = card.findViewById<LinearLayout>(R.id.llExpandedContent)
            val ivChevron = card.findViewById<ImageView>(R.id.ivChevron)
            val llCardHeader = card.findViewById<LinearLayout>(R.id.llCardHeader)
            tvUserName.text = verif.user?.fullName ?: ""; tvUserEmail.text = verif.user?.email ?: ""; tvUserRole.text = verif.user?.role ?: ""; tvVerifStatus.text = verif.status
            when (verif.status.uppercase()) {
                "PENDING" -> { tvVerifStatus.setBackgroundResource(R.drawable.badge_pending); tvVerifStatus.setTextColor(getColor(R.color.white)) }
                "APPROVED" -> { tvVerifStatus.setBackgroundResource(R.drawable.badge_available); tvVerifStatus.setTextColor(getColor(R.color.white)) }
                "REJECTED" -> { tvVerifStatus.setBackgroundResource(R.drawable.badge_declined); tvVerifStatus.setTextColor(getColor(R.color.white)) }
            }
            val profileUrl = verif.user?.profileImageUrl
            if (!profileUrl.isNullOrEmpty()) {
                Glide.with(this).load(if (profileUrl.startsWith("http")) profileUrl else "$baseUrl$profileUrl").circleCrop().into(ivProfileImage)
            } else {
                ivProfileImage.setImageResource(R.drawable.pawlogo2)
            }
            llExpandedContent.visibility = View.GONE; ivChevron.visibility = View.GONE
            val idUrl = verif.idImageUrl
            llCardHeader.setOnClickListener {
                val dialogView = LinearLayout(this@AdminDashboardActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 48, 48, 48) }
                val headerLayout = LinearLayout(this@AdminDashboardActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 40 } }
                val profileIv = ImageView(this@AdminDashboardActivity).apply { layoutParams = LinearLayout.LayoutParams(140, 140).also { it.marginEnd = 32 }; scaleType = ImageView.ScaleType.CENTER_CROP; setBackgroundResource(R.drawable.chip_inactive); clipToOutline = true }
                if (!profileUrl.isNullOrEmpty()) {
                    Glide.with(this@AdminDashboardActivity).load(if (profileUrl.startsWith("http")) profileUrl else "$baseUrl$profileUrl").circleCrop().into(profileIv)
                } else {
                    profileIv.setImageResource(R.drawable.pawlogo2)
                }
                headerLayout.addView(profileIv)
                val infoLayout = LinearLayout(this@AdminDashboardActivity).apply { orientation = LinearLayout.VERTICAL }
                val nameRoleLayout = LinearLayout(this@AdminDashboardActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 12 } }
                nameRoleLayout.addView(TextView(this@AdminDashboardActivity).apply { text = verif.user?.fullName ?: ""; textSize = 18f; setTypeface(null, android.graphics.Typeface.BOLD); setTextColor(getColor(R.color.dark)); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.marginEnd = 16 } })
                nameRoleLayout.addView(TextView(this@AdminDashboardActivity).apply { text = if (verif.user?.role == "ADOPTER") "Adopter" else "Pet Owner"; textSize = 10f; setTextColor(getColor(R.color.white)); setBackgroundResource(if (verif.user?.role == "ADOPTER") R.drawable.badge_available else R.drawable.badge_pending); setPadding(20, 6, 20, 6) })
                infoLayout.addView(nameRoleLayout)
                infoLayout.addView(TextView(this@AdminDashboardActivity).apply { text = "✉  ${verif.user?.email}"; textSize = 13f; setTextColor(getColor(R.color.muted)); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 8 } })
                infoLayout.addView(TextView(this@AdminDashboardActivity).apply { text = "✆  ${verif.user?.phoneNumber?.takeIf { it.isNotBlank() } ?: "No phone"}"; textSize = 13f; setTextColor(getColor(R.color.muted)); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 8 } })
                infoLayout.addView(TextView(this@AdminDashboardActivity).apply { text = "⌂  ${verif.user?.address?.takeIf { it.isNotBlank() } ?: "Location not provided"}"; textSize = 13f; setTextColor(getColor(R.color.muted)) })
                headerLayout.addView(infoLayout); dialogView.addView(headerLayout)
                dialogView.addView(TextView(this@AdminDashboardActivity).apply { text = "Reason: ${verif.reason ?: "None"}"; textSize = 14f; setTextColor(getColor(R.color.dark)); setPadding(20, 20, 20, 20); setBackgroundResource(R.drawable.chip_inactive); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = 24 } })
                val imageView = ImageView(this@AdminDashboardActivity).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 800).also { it.bottomMargin = 24 }; scaleType = ImageView.ScaleType.FIT_CENTER }
                if (!idUrl.isNullOrEmpty()) Glide.with(this@AdminDashboardActivity).load(if (idUrl.startsWith("http")) idUrl else "$baseUrl$idUrl").into(imageView)
                dialogView.addView(imageView)
                if (verif.status.uppercase() == "PENDING") {
                    val actionRow = LinearLayout(this@AdminDashboardActivity).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) }
                    val btnReject = Button(this@AdminDashboardActivity).apply { text = "✕ Reject"; setTextColor(getColor(R.color.white)); backgroundTintList = getColorStateList(R.color.terracotta); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also { it.marginEnd = 16 }; setOnClickListener { showRejectVerifDialog(verif.id ?: 0) } }
                    val btnApprove = Button(this@AdminDashboardActivity).apply { text = "✓ Approve"; setTextColor(getColor(R.color.white)); backgroundTintList = getColorStateList(R.color.green); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); setOnClickListener { CoroutineScope(Dispatchers.IO).launch { try { val res = RetrofitClient.instance.approveVerification(token, verif.id ?: 0); withContext(Dispatchers.Main) { if (res.isSuccessful) { Toast.makeText(this@AdminDashboardActivity, "Verification approved!", Toast.LENGTH_SHORT).show(); loadVerifications() } } } catch (_: Exception) {} } } }
                    actionRow.addView(btnReject); actionRow.addView(btnApprove); dialogView.addView(actionRow)
                }
                val scrollView = android.widget.ScrollView(this@AdminDashboardActivity).apply { addView(dialogView) }
                android.app.AlertDialog.Builder(this@AdminDashboardActivity).setView(scrollView).setPositiveButton("Close", null).show()
            }
            listContainer.addView(card)
        }
    }

    private fun showRejectVerifDialog(verifId: Int) {
        val selectedReasons = mutableSetOf<String>()
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 32, 48, 16) }
        val scrollView = android.widget.ScrollView(this)
        val reasonsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val customInput = EditText(this).apply { hint = "Enter custom reason..."; setPadding(0, 16, 0, 0); visibility = View.GONE }
        verifRejectReasons.forEach { reason ->
            val chip = TextView(this).apply { text = reason; setPadding(32, 20, 32, 20); textSize = 14f; setBackgroundResource(R.drawable.chip_inactive); setTextColor(getColor(R.color.dark)); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, 8, 0, 8) } }
            chip.setOnClickListener {
                if (selectedReasons.contains(reason)) { selectedReasons.remove(reason); chip.setBackgroundResource(R.drawable.chip_inactive); chip.setTextColor(getColor(R.color.dark)) }
                else { selectedReasons.add(reason); chip.setBackgroundResource(R.drawable.chip_active); chip.setTextColor(getColor(R.color.white)) }
                if (reason == "Others...") customInput.visibility = if (selectedReasons.contains("Others...")) View.VISIBLE else View.GONE
            }
            reasonsContainer.addView(chip)
        }
        reasonsContainer.addView(customInput); scrollView.addView(reasonsContainer); dialogView.addView(scrollView)
        android.app.AlertDialog.Builder(this).setTitle("Reject Verification").setMessage("Select one or more reasons:").setView(dialogView)
            .setPositiveButton("Reject") { _, _ ->
                val reasons = selectedReasons.filter { it != "Others..." }.toMutableList()
                if (selectedReasons.contains("Others...") && customInput.text.toString().trim().isNotEmpty()) reasons.add(customInput.text.toString().trim())
                if (reasons.isEmpty()) { Toast.makeText(this, "Please select at least one reason.", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val res = RetrofitClient.instance.rejectVerification(token, verifId, mapOf("adminComment" to reasons.joinToString("; ")))
                        withContext(Dispatchers.Main) { if (res.isSuccessful) { Toast.makeText(this@AdminDashboardActivity, "Verification rejected.", Toast.LENGTH_SHORT).show(); loadVerifications() } }
                    } catch (_: Exception) {}
                }
            }.setNegativeButton("Cancel", null).show()
    }
    private fun switchToReportsSection() {
        currentSection = "REPORTS"
        setNavActive("REPORTS")
        showFilterTabs()
        tabRejected.visibility = View.GONE  // no more Ignored tab
        reportStatusFilter = "ALL"
        tabPending.text = "Pending"
        tabApproved.text = "Resolved"
        tabAll.text = "All"
        listOf(tabPending, tabApproved, tabAll).forEach {
            it.setBackgroundResource(R.drawable.chip_inactive)
            it.setTextColor(getColor(R.color.dark))
        }
        tabAll.setBackgroundResource(R.drawable.chip_active)
        tabAll.setTextColor(getColor(R.color.white))
        tabAll.setOnClickListener {
            reportStatusFilter = "ALL"
            updateReportStats()
            buildReportsList(allReports)
        }
        tabPending.setOnClickListener {
            reportStatusFilter = "PENDING"
            updateReportStats()
            buildReportsList(allReports.filter { it.status.uppercase() == "PENDING" })
        }
        tabApproved.setOnClickListener {
            reportStatusFilter = "RESOLVED"
            updateReportStats()
            buildReportsList(allReports.filter { it.status.uppercase() == "RESOLVED" || it.reportedUserBanned })
        }
        loadReports()
    }

    private fun loadReports() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllReports(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        allReports = response.body()?.data ?: emptyList()
                        if (currentSection == "REPORTS") {
                            updateReportStats()
                            val filtered = when (reportStatusFilter) {
                                "PENDING"  -> allReports.filter { it.status.uppercase() == "PENDING" }
                                "RESOLVED" -> allReports.filter { it.status.uppercase() == "RESOLVED" || it.reportedUserBanned }
                                else       -> allReports
                            }
                            buildReportsList(filtered)
                        }
                        updateNavBadges()
                    } else {
                        Toast.makeText(this@AdminDashboardActivity, "Failed to load reports: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminDashboardActivity, "Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildReportsList(reports: List<ReportItem>) {
        llReportsList.removeAllViews()
        if (reports.isEmpty()) {
            tvEmpty.text = "No reports found."
            tvEmpty.visibility = View.VISIBLE
            return
        }
        tvEmpty.visibility = View.GONE
        reports.forEach { report ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.card_background)
                setPadding(40, 40, 40, 40)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 24 }
            }

            // Header row: Report #id + status badge
            val headerRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 16 }
            }
            headerRow.addView(TextView(this).apply {
                text = "Report #${report.id}"
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.dark))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            headerRow.addView(TextView(this).apply {
                text = report.status
                textSize = 11f
                setTextColor(getColor(R.color.white))
                setBackgroundResource(when (report.status.uppercase()) {
                    "RESOLVED" -> R.drawable.badge_available
                    "IGNORED" -> R.drawable.chip_inactive
                    else -> R.drawable.badge_pending
                })
                if (report.status.uppercase() == "IGNORED") setTextColor(getColor(R.color.muted))
                setPadding(24, 8, 24, 8)
            })
            card.addView(headerRow)

            // Reporter
            card.addView(TextView(this).apply {
                text = "Reporter: ${report.reporterName} (${report.reporterEmail})"
                textSize = 13f
                setTextColor(getColor(R.color.dark))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 8 }
            })

            // Reported user
            card.addView(TextView(this).apply {
                text = "Reported User: ${report.reportedUserName} (${report.reportedUserEmail})"
                textSize = 13f
                setTextColor(getColor(R.color.dark))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 8 }
            })

            card.addView(LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.chip_inactive)
                setPadding(24, 16, 24, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 16 }
                addView(TextView(this@AdminDashboardActivity).apply {
                    text = "Reason: ${report.reason}"
                    textSize = 13f
                    setTextColor(getColor(R.color.dark))
                })
            })

            card.setOnClickListener {
                val intent = Intent(this, ReportDetailsActivity::class.java).apply {
                    putExtra("reportId", report.id)
                    putExtra("reportedUserId", report.reportedUserId)
                    putExtra("status", report.status)
                    putExtra("reporterName", report.reporterName)
                    putExtra("reporterEmail", report.reporterEmail)
                    putExtra("reporterRole", report.reporterRole)
                    putExtra("reportedUserName", report.reportedUserName)
                    putExtra("reportedUserEmail", report.reportedUserEmail)
                    putExtra("reportedUserBanned", report.reportedUserBanned)
                    putExtra("reason", report.reason)
                    putExtra("createdAt", report.createdAt)
                    val ar = report.adoptionRequest
                    if (ar != null) {
                        putExtra("arAdopterName", ar.adopterName)
                        putExtra("arContact", ar.contactInfo)
                        putExtra("arReason", ar.reason)
                        putExtra("arNote", ar.noteToOwner ?: "")
                    }
                    putExtra("hasAdoptionRequest", report.adoptionRequest != null)
                }
                startActivity(intent)
            }

            llReportsList.addView(card)
        }

    }
}