package com.mobile.pawpal.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.features.adoption.AdopterDashboardActivity
import com.mobile.pawpal.features.adoption.MyRequestsActivity
import com.mobile.pawpal.shared.AdoptionRequestItem
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfileImage: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: Spinner
    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: TextView
    private lateinit var viewForm: LinearLayout
    private lateinit var viewDisplay: LinearLayout
    private lateinit var tvDisplayPhone: TextView
    private lateinit var tvDisplayAddress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvStatTotal: TextView
    private lateinit var tvStatPending: TextView
    private lateinit var tvStatApproved: TextView
    private lateinit var tvStatDeclined: TextView
    private lateinit var tvRequestCount: TextView
    private lateinit var llRequestGrid: LinearLayout
    private lateinit var tvNoRequests: TextView

    private lateinit var navBrowse: FrameLayout
    private lateinit var navRequests: FrameLayout
    private lateinit var navProfile: FrameLayout
    private lateinit var navIconBrowse: ImageView
    private lateinit var navIconRequests: ImageView
    private lateinit var navIconProfile: ImageView
    private lateinit var navLabelBrowse: TextView
    private lateinit var navLabelRequests: TextView
    private lateinit var navLabelProfile: TextView

    private var token = ""
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val locations = listOf(
        "Select your city/municipality",
        "Cebu City", "Mandaue", "Lapu-Lapu", "Talisay", "Danao", "Carcar",
        "Toledo", "Naga", "Bogo", "Minglanilla", "San Fernando", "Consolacion",
        "Liloan", "Compostela", "Cordova", "Moalboal", "Oslob", "Alcoy",
        "Dalaguete", "Others"
    )

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).circleCrop().into(ivProfileImage)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let {
                selectedImageUri = it
                Glide.with(this).load(it).circleCrop().into(ivProfileImage)
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"

        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        btnEdit = findViewById(R.id.btnEdit)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        viewForm = findViewById(R.id.viewForm)
        viewDisplay = findViewById(R.id.viewDisplay)
        tvDisplayPhone = findViewById(R.id.tvDisplayPhone)
        tvDisplayAddress = findViewById(R.id.tvDisplayAddress)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tvStatTotal = findViewById(R.id.tvStatTotal)
        tvStatPending = findViewById(R.id.tvStatPending)
        tvStatApproved = findViewById(R.id.tvStatApproved)
        tvStatDeclined = findViewById(R.id.tvStatDeclined)
        tvRequestCount = findViewById(R.id.tvRequestCount)
        llRequestGrid = findViewById(R.id.llRequestGrid)
        tvNoRequests = findViewById(R.id.tvNoRequests)

        navBrowse = findViewById(R.id.navBrowse)
        navRequests = findViewById(R.id.navRequests)
        navProfile = findViewById(R.id.navProfile)
        navIconBrowse = findViewById(R.id.navIconBrowse)
        navIconRequests = findViewById(R.id.navIconRequests)
        navIconProfile = findViewById(R.id.navIconProfile)
        navLabelBrowse = findViewById(R.id.navLabelBrowse)
        navLabelRequests = findViewById(R.id.navLabelRequests)
        navLabelProfile = findViewById(R.id.navLabelProfile)

        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        etAddress.adapter = locationAdapter

        setNavActive("PROFILE")

        navBrowse.setOnClickListener {
            val intent = Intent(this, AdopterDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        navRequests.setOnClickListener {
            val intent = Intent(this, MyRequestsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        navProfile.setOnClickListener { }

        btnEdit.setOnClickListener {
            viewDisplay.visibility = View.GONE
            viewForm.visibility = View.VISIBLE
            btnEdit.visibility = View.GONE
        }

        ivProfileImage.isClickable = true
        ivProfileImage.setOnClickListener { showImagePickerDialog() }

        btnCancel.setOnClickListener {
            viewForm.visibility = View.GONE
            viewDisplay.visibility = View.VISIBLE
            btnEdit.visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = if (etAddress.selectedItemPosition == 0) "" else etAddress.selectedItem.toString()
            if (fullName.isEmpty()) {
                tvError.text = "Full name is required."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            tvError.visibility = View.GONE
            saveProfile(fullName, phone, address)
        }

        loadProfile()
        loadRequests()
    }

    private fun setNavActive(section: String) {
        val green = getColor(R.color.green)
        val muted = getColor(R.color.muted)
        navLabelBrowse.setTextColor(muted)
        navIconBrowse.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelRequests.setTextColor(muted)
        navIconRequests.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelProfile.setTextColor(muted)
        navIconProfile.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        when (section) {
            "BROWSE" -> { navLabelBrowse.setTextColor(green); navIconBrowse.setColorFilter(green) }
            "REQUESTS" -> { navLabelRequests.setTextColor(green); navIconRequests.setColorFilter(green) }
            "PROFILE" -> { navLabelProfile.setTextColor(green); navIconProfile.setColorFilter(green) }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        android.app.AlertDialog.Builder(this)
            .setTitle("Update Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            launchCamera()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    1 -> galleryLauncher.launch("image/*")
                }
            }.show()
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile(
            "profile_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}",
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        cameraImageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        cameraLauncher.launch(cameraImageUri!!)
    }
    private fun loadProfile() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getProfile(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val profile = response.body()!!.data!!
                        tvFullName.text = profile.fullName
                        tvEmail.text = profile.email
                        tvRole.text = profile.role.replace("_", " ")
                        tvDisplayPhone.text = if (profile.phoneNumber.isNullOrEmpty()) "Not set" else profile.phoneNumber
                        tvDisplayAddress.text = if (profile.address.isNullOrEmpty()) "Not set" else profile.address
                        etFullName.setText(profile.fullName)
                        etPhone.setText(profile.phoneNumber ?: "")
                        val locationIndex = locations.indexOf(profile.address ?: "")
                        if (locationIndex >= 0) etAddress.setSelection(locationIndex)
                        if (!profile.profileImageUrl.isNullOrEmpty()) {
                            val fullUrl = if (profile.profileImageUrl.startsWith("http")) profile.profileImageUrl
                            else "https://net-vanquish-poise.ngrok-free.dev${profile.profileImageUrl}"
                            Glide.with(this@ProfileActivity).load(fullUrl).circleCrop().into(ivProfileImage)
                        }
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to load profile.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ProfileActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadRequests() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getMyRequests(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val requests = response.body()?.data ?: listOf()
                        val total = requests.size
                        val pending = requests.count { it.status.uppercase() == "PENDING" }
                        val approved = requests.count { it.status.uppercase() == "APPROVED" }
                        val declined = requests.count { it.status.uppercase() == "DECLINED" }
                        tvStatTotal.text = total.toString()
                        tvStatPending.text = pending.toString()
                        tvStatApproved.text = approved.toString()
                        tvStatDeclined.text = declined.toString()
                        tvRequestCount.text = "$total Total"
                        if (requests.isEmpty()) {
                            tvNoRequests.visibility = View.VISIBLE
                            llRequestGrid.visibility = View.GONE
                        } else {
                            tvNoRequests.visibility = View.GONE
                            llRequestGrid.visibility = View.VISIBLE
                            buildRequestGrid(requests)
                        }
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun buildRequestGrid(requests: List<AdoptionRequestItem>) {
        llRequestGrid.removeAllViews()
        var rowLayout: LinearLayout? = null
        requests.forEachIndexed { index, request ->
            if (index % 2 == 0) {
                rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 12 }
                }
                llRequestGrid.addView(rowLayout)
            }
            val card = LayoutInflater.from(this).inflate(R.layout.item_profile_request_card, rowLayout, false)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            if (index % 2 == 0) lp.marginEnd = 8 else lp.marginStart = 8
            card.layoutParams = lp

            val ivPet = card.findViewById<ImageView>(R.id.ivPetImage)
            val tvName = card.findViewById<TextView>(R.id.tvPetName)
            val tvBreed = card.findViewById<TextView>(R.id.tvBreed)
            val tvStatus = card.findViewById<TextView>(R.id.tvStatus)
            val tvDate = card.findViewById<TextView>(R.id.tvDate)

            tvName.text = request.pet.name
            tvBreed.text = request.pet.breed
            tvDate.text = formatDate(request.createdAt)
            tvStatus.text = request.status

            when (request.status.uppercase()) {
                "APPROVED" -> {
                    tvStatus.setBackgroundResource(R.drawable.badge_available)
                    tvStatus.setTextColor(getColor(R.color.white))
                }
                "PENDING" -> {
                    tvStatus.setBackgroundResource(R.drawable.badge_pending)
                    tvStatus.setTextColor(getColor(R.color.white))
                }
                "DECLINED" -> {
                    tvStatus.setBackgroundResource(R.drawable.badge_declined)
                    tvStatus.setTextColor(getColor(R.color.white))
                }
            }

            if (!request.pet.imageUrl.isNullOrEmpty()) {
                val fullUrl = if (request.pet.imageUrl.startsWith("http")) request.pet.imageUrl
                else "https://net-vanquish-poise.ngrok-free.dev${request.pet.imageUrl}"
                Glide.with(this).load(fullUrl).centerCrop().into(ivPet)
            } else {
                ivPet.setImageResource(R.drawable.pawlogo2)
            }

            rowLayout?.addView(card)
        }

        if (requests.size % 2 != 0) {
            val spacer = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 1, 1f).also { it.marginStart = 8 }
            }
            rowLayout?.addView(spacer)
        }
    }

    private fun saveProfile(fullName: String, phone: String, address: String) {
        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val plain = "text/plain".toMediaTypeOrNull()
                val imagePart = selectedImageUri?.let { uri ->
                    val stream = contentResolver.openInputStream(uri)
                    val bytes = stream?.readBytes() ?: return@let null
                    stream.close()
                    val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", "profile.jpg", requestBody)
                }
                val response = RetrofitClient.instance.updateProfile(
                    token = token,
                    fullName = fullName.toRequestBody(plain),
                    phoneNumber = if (phone.isEmpty()) null else phone.toRequestBody(plain),
                    address = if (address.isEmpty()) null else address.toRequestBody(plain),
                    bio = null,
                    profileImage = imagePart
                )
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        selectedImageUri = null
                        viewForm.visibility = View.GONE
                        viewDisplay.visibility = View.VISIBLE
                        btnEdit.visibility = View.VISIBLE
                        loadProfile()
                    } else {
                        tvError.text = response.body()?.error?.message ?: "Failed to update."
                        tvError.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    tvError.text = "Connection error."
                    tvError.visibility = View.VISIBLE
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