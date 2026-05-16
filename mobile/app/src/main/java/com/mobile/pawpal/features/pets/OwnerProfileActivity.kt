package com.mobile.pawpal.features.pets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.Pet
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OwnerProfileActivity : AppCompatActivity() {

    private lateinit var ivProfileImage: ImageView
    private lateinit var tvFullName: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var btnEdit: Button
    private lateinit var tvStatAvailable: TextView
    private lateinit var tvStatPending: TextView
    private lateinit var tvStatAdopted: TextView
    private lateinit var tvPetCount: TextView
    private lateinit var llPetList: LinearLayout
    private lateinit var tvNoPets: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewDisplay: LinearLayout
    private lateinit var viewForm: LinearLayout
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnCancel: TextView
    private lateinit var tvError: TextView

    private lateinit var navMyPets: FrameLayout
    private lateinit var navPostPet: FrameLayout
    private lateinit var navOwnerProfile: FrameLayout
    private lateinit var navIconMyPets: ImageView
    private lateinit var navIconPostPet: ImageView
    private lateinit var navIconOwnerProfile: ImageView
    private lateinit var navLabelMyPets: TextView
    private lateinit var navLabelPostPet: TextView
    private lateinit var navLabelOwnerProfile: TextView

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
        setContentView(R.layout.activity_owner_profile)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"

        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvFullName = findViewById(R.id.tvFullName)
        tvRole = findViewById(R.id.tvRole)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        btnEdit = findViewById(R.id.btnEdit)
        tvStatAvailable = findViewById(R.id.tvStatAvailable)
        tvStatPending = findViewById(R.id.tvStatPending)
        tvStatAdopted = findViewById(R.id.tvStatAdopted)
        tvPetCount = findViewById(R.id.tvPetCount)
        llPetList = findViewById(R.id.llPetList)
        tvNoPets = findViewById(R.id.tvNoPets)
        progressBar = findViewById(R.id.progressBar)
        viewDisplay = findViewById(R.id.viewDisplay)
        viewForm = findViewById(R.id.viewForm)
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        tvError = findViewById(R.id.tvError)

        navMyPets = findViewById(R.id.navMyPets)
        navPostPet = findViewById(R.id.navPostPet)
        navOwnerProfile = findViewById(R.id.navOwnerProfile)
        navIconMyPets = findViewById(R.id.navIconMyPets)
        navIconPostPet = findViewById(R.id.navIconPostPet)
        navIconOwnerProfile = findViewById(R.id.navIconOwnerProfile)
        navLabelMyPets = findViewById(R.id.navLabelMyPets)
        navLabelPostPet = findViewById(R.id.navLabelPostPet)
        navLabelOwnerProfile = findViewById(R.id.navLabelOwnerProfile)

        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        etAddress.adapter = locationAdapter

        setNavActive("PROFILE")

        navMyPets.setOnClickListener {
            val intent = Intent(this, OwnerDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        navPostPet.setOnClickListener {
            startActivity(Intent(this, PostPetActivity::class.java))
        }
        navOwnerProfile.setOnClickListener { }

        btnEdit.setOnClickListener {
            viewDisplay.visibility = View.GONE
            viewForm.visibility = View.VISIBLE
        }

        ivProfileImage.isClickable = true
        ivProfileImage.setOnClickListener { showImagePickerDialog() }

        btnCancel.setOnClickListener {
            viewForm.visibility = View.GONE
            viewDisplay.visibility = View.VISIBLE
        }
        btnSave.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = if (etAddress.selectedItemPosition == 0) "" else etAddress.selectedItem.toString()
            if (name.isEmpty()) {
                tvError.text = "Full name is required."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            tvError.visibility = View.GONE
            saveProfile(name, phone, address)
        }

        loadProfile()
        loadMyPets()
    }

    private fun setNavActive(section: String) {
        val green = getColor(R.color.green)
        val muted = getColor(R.color.muted)
        navLabelMyPets.setTextColor(muted)
        navIconMyPets.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelPostPet.setTextColor(muted)
        navIconPostPet.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelOwnerProfile.setTextColor(muted)
        navIconOwnerProfile.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        when (section) {
            "MY_PETS" -> { navLabelMyPets.setTextColor(green); navIconMyPets.setColorFilter(green) }
            "POST" -> { navLabelPostPet.setTextColor(green); navIconPostPet.setColorFilter(green) }
            "PROFILE" -> { navLabelOwnerProfile.setTextColor(green); navIconOwnerProfile.setColorFilter(green) }
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getProfile(token)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val p = response.body()!!.data!!
                        tvFullName.text = p.fullName
                        tvRole.text = p.role.replace("_", " ")
                        tvEmail.text = p.email
                        tvPhone.text = if (p.phoneNumber.isNullOrEmpty()) "Not set" else p.phoneNumber
                        etFullName.setText(p.fullName)
                        etPhone.setText(p.phoneNumber ?: "")
                        val locationIndex = locations.indexOf(p.address ?: "")
                        if (locationIndex >= 0) etAddress.setSelection(locationIndex)
                        if (!p.profileImageUrl.isNullOrEmpty()) {
                            val url = if (p.profileImageUrl.startsWith("http")) p.profileImageUrl
                            else "https://net-vanquish-poise.ngrok-free.dev${p.profileImageUrl}"
                            Glide.with(this@OwnerProfileActivity).load(url).circleCrop().into(ivProfileImage)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadMyPets() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getMyPets(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val pets = response.body()?.data?.pets ?: listOf()
                        val available = pets.count { it.status.uppercase() == "AVAILABLE" }
                        val pending = pets.count { it.status.uppercase() == "UNDER_REVIEW" }
                        val adopted = pets.count { it.status.uppercase() == "ADOPTED" }
                        tvStatAvailable.text = available.toString()
                        tvStatPending.text = pending.toString()
                        tvStatAdopted.text = adopted.toString()
                        tvPetCount.text = "${pets.size} Total"
                        if (pets.isEmpty()) {
                            tvNoPets.visibility = View.VISIBLE
                            llPetList.visibility = View.GONE
                        } else {
                            tvNoPets.visibility = View.GONE
                            llPetList.visibility = View.VISIBLE
                            buildPetList(pets)
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
            }
        }
    }

    private fun buildPetList(pets: List<Pet>) {
        llPetList.removeAllViews()
        pets.forEach { pet ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_owner_profile_pet_row, llPetList, false)
            val iv = row.findViewById<ImageView>(R.id.ivPetThumb)
            val tvName = row.findViewById<TextView>(R.id.tvPetName)
            val tvBreed = row.findViewById<TextView>(R.id.tvBreed)
            val tvStatus = row.findViewById<TextView>(R.id.tvStatus)
            val tvAge = row.findViewById<TextView>(R.id.tvAge)

            tvName.text = pet.name
            tvBreed.text = pet.breed
            tvAge.text = pet.age
            tvStatus.text = pet.status.replace("_", " ")

            when (pet.status.uppercase()) {
                "AVAILABLE" -> { tvStatus.setBackgroundResource(R.drawable.badge_available); tvStatus.setTextColor(getColor(R.color.white)) }
                "UNDER_REVIEW" -> { tvStatus.setBackgroundResource(R.drawable.badge_pending); tvStatus.setTextColor(getColor(R.color.white)) }
                "ADOPTED" -> { tvStatus.setBackgroundResource(R.drawable.badge_adopted); tvStatus.setTextColor(getColor(R.color.white)) }
                "REJECTED" -> { tvStatus.setBackgroundResource(R.drawable.badge_declined); tvStatus.setTextColor(getColor(R.color.white)) }
            }

            if (!pet.imageUrl.isNullOrEmpty()) {
                val url = if (pet.imageUrl.startsWith("http")) pet.imageUrl
                else "https://net-vanquish-poise.ngrok-free.dev${pet.imageUrl}"
                Glide.with(this).load(url).centerCrop().into(iv)
            } else {
                iv.setImageResource(R.drawable.pawlogo2)
            }

            llPetList.addView(row)

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    .also { it.topMargin = 8; it.bottomMargin = 8 }
                setBackgroundColor(getColor(R.color.peach))
            }
            llPetList.addView(divider)
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
                        Toast.makeText(this@OwnerProfileActivity, "Profile updated!", Toast.LENGTH_SHORT).show()
                        selectedImageUri = null
                        viewForm.visibility = View.GONE
                        viewDisplay.visibility = View.VISIBLE
                        loadProfile()
                    } else {
                        tvError.text = "Failed to update."
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
}