package com.mobile.pawpal.features.pets

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.mobile.pawpal.R
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class EditPetActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var etBreed: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var etAge: EditText
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var ivPetImage: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var llTraits: LinearLayout
    private lateinit var btnBack: TextView

    private var selectedImageUri: Uri? = null
    private val selectedTraits = mutableSetOf<String>()
    private var vaccinated = false
    private var neutered = false
    private var microchipped = false
    private var healthChecked = false
    private var token = ""
    private var petId = -1

    private val traits = listOf("Friendly", "Energetic", "Playful", "Good with Kids",
        "Good with Dogs", "Good with Cats", "House-trained", "Calm",
        "Loyal", "Curious", "Affectionate", "Independent")

    private val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit", "Other")
    private val genders = listOf("Unknown", "Male", "Female")

    private val IMAGE_PICK_CODE = 1001
    private val traitChips = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pet)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"
        petId = intent.getIntExtra("petId", -1)

        if (petId == -1) { finish(); return }

        etName = findViewById(R.id.etName)
        spinnerType = findViewById(R.id.spinnerType)
        etBreed = findViewById(R.id.etBreed)
        spinnerGender = findViewById(R.id.spinnerGender)
        etAge = findViewById(R.id.etAge)
        etDescription = findViewById(R.id.etDescription)
        etLocation = findViewById(R.id.etLocation)
        ivPetImage = findViewById(R.id.ivPetImage)
        btnPickImage = findViewById(R.id.btnPickImage)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        llTraits = findViewById(R.id.llTraits)
        btnBack = findViewById(R.id.btnBack)

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, petTypes)
        spinnerGender.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)

        buildTraitChips()
        buildHealthCards()

        btnBack.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnSubmit.setOnClickListener { submitEdit() }

        loadPetData()
    }

    private fun loadPetData() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getPetById(token, petId)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val pet = response.body()!!.data!!
                        etName.setText(pet.name)
                        etBreed.setText(pet.breed)
                        etAge.setText(pet.age)
                        etDescription.setText(pet.description ?: "")
                        etLocation.setText(pet.location)

                        val typeIndex = petTypes.indexOfFirst { it.uppercase() == pet.type?.uppercase() }
                        if (typeIndex >= 0) spinnerType.setSelection(typeIndex)

                        val genderIndex = genders.indexOfFirst { it.uppercase() == pet.gender?.uppercase() }
                        if (genderIndex >= 0) spinnerGender.setSelection(genderIndex)

                        vaccinated = pet.vaccinated
                        neutered = pet.neutered
                        microchipped = pet.microchipped
                        healthChecked = pet.healthChecked

                        refreshHealthCards()

                        pet.characteristics?.forEach { trait ->
                            selectedTraits.add(trait)
                        }
                        refreshTraitChips()

                        if (!pet.imageUrl.isNullOrEmpty()) {
                            val fullUrl = if (pet.imageUrl.startsWith("http")) pet.imageUrl
                            else "https://net-vanquish-poise.ngrok-free.dev${pet.imageUrl}"
                            Glide.with(this@EditPetActivity).load(fullUrl).centerCrop().into(ivPetImage)
                        }
                    } else {
                        Toast.makeText(this@EditPetActivity, "Failed to load pet.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@EditPetActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun buildTraitChips() {
        var rowLayout: LinearLayout? = null
        traits.forEachIndexed { index, trait ->
            if (index % 3 == 0) {
                rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 8 }
                }
                llTraits.addView(rowLayout)
            }
            val chip = TextView(this).apply {
                text = trait
                textSize = 12f
                setPadding(20, 10, 20, 10)
                setTextColor(getColor(R.color.green))
                setBackgroundResource(R.drawable.chip_inactive)
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                lp.marginEnd = 8
                layoutParams = lp
            }
            chip.setOnClickListener {
                if (selectedTraits.contains(trait)) {
                    selectedTraits.remove(trait)
                    chip.setTextColor(getColor(R.color.green))
                    chip.setBackgroundResource(R.drawable.chip_inactive)
                } else {
                    selectedTraits.add(trait)
                    chip.setTextColor(getColor(R.color.white))
                    chip.setBackgroundResource(R.drawable.chip_active)
                }
            }
            traitChips.add(chip)
            rowLayout?.addView(chip)
        }
    }

    private fun refreshTraitChips() {
        traitChips.forEachIndexed { index, chip ->
            val trait = traits[index]
            if (selectedTraits.contains(trait)) {
                chip.setTextColor(getColor(R.color.white))
                chip.setBackgroundResource(R.drawable.chip_active)
            } else {
                chip.setTextColor(getColor(R.color.green))
                chip.setBackgroundResource(R.drawable.chip_inactive)
            }
        }
    }

    private val healthCardViews = mutableListOf<View>()
    private val healthKeys = listOf("cbVaccinated", "cbNeutered", "cbMicrochipped", "cbHealthChecked")

    private fun buildHealthCards() {
        val container = findViewById<LinearLayout>(R.id.llHealthCards)
        val healthOptions = listOf(
            Triple("cbVaccinated", "Vaccinated", "Up to date on all shots"),
            Triple("cbNeutered", "Neutered / Spayed", "Spayed/neutered"),
            Triple("cbMicrochipped", "Microchipped", "Registered microchip"),
            Triple("cbHealthChecked", "Health Check", "Recent vet examination")
        )

        var rowLayout: LinearLayout? = null
        healthOptions.forEachIndexed { index, (key, title, subtitle) ->
            if (index % 2 == 0) {
                rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 8 }
                }
                container.addView(rowLayout)
            }
            val card = layoutInflater.inflate(R.layout.item_health_toggle, rowLayout, false)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            if (index % 2 == 0) lp.marginEnd = 8
            card.layoutParams = lp
            card.findViewById<TextView>(R.id.tvHealthTitle).text = title
            card.findViewById<TextView>(R.id.tvHealthSubtitle).text = subtitle
            card.setOnClickListener {
                when (key) {
                    "cbVaccinated" -> { vaccinated = !vaccinated; updateHealthCard(card, vaccinated) }
                    "cbNeutered" -> { neutered = !neutered; updateHealthCard(card, neutered) }
                    "cbMicrochipped" -> { microchipped = !microchipped; updateHealthCard(card, microchipped) }
                    "cbHealthChecked" -> { healthChecked = !healthChecked; updateHealthCard(card, healthChecked) }
                }
            }
            healthCardViews.add(card)
            rowLayout?.addView(card)
        }
    }

    private fun refreshHealthCards() {
        val states = listOf(vaccinated, neutered, microchipped, healthChecked)
        healthCardViews.forEachIndexed { index, card ->
            updateHealthCard(card, states[index])
        }
    }

    private fun updateHealthCard(card: View, selected: Boolean) {
        card.setBackgroundResource(if (selected) R.drawable.chip_active else R.drawable.chip_inactive)
        card.findViewById<TextView>(R.id.tvHealthTitle).setTextColor(
            if (selected) getColor(R.color.white) else getColor(R.color.dark)
        )
        card.findViewById<TextView>(R.id.tvHealthSubtitle).setTextColor(
            if (selected) getColor(R.color.white) else getColor(R.color.muted)
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Glide.with(this).load(selectedImageUri).centerCrop().into(ivPetImage)
        }
    }

    private fun submitEdit() {
        val name = etName.text.toString().trim()
        val type = petTypes[spinnerType.selectedItemPosition]
        val breed = etBreed.text.toString().trim()
        val gender = genders[spinnerGender.selectedItemPosition]
        val age = etAge.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (name.isEmpty() || age.isEmpty() || description.isEmpty() || location.isEmpty()) {
            tvError.text = "Please fill in all required fields."
            tvError.visibility = View.VISIBLE
            return
        }

        tvError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        val petData = mapOf(
            "name" to name,
            "type" to type.uppercase(),
            "breed" to breed,
            "gender" to gender.uppercase(),
            "age" to age,
            "description" to description,
            "location" to location,
            "characteristics" to selectedTraits.toList(),
            "vaccinated" to vaccinated,
            "neutered" to neutered,
            "microchipped" to microchipped,
            "healthChecked" to healthChecked
        )

        val json = Gson().toJson(petData)
        val dataBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null
        if (selectedImageUri != null) {
            val stream = contentResolver.openInputStream(selectedImageUri!!)
            val bytes = stream?.readBytes()
            stream?.close()
            if (bytes != null) {
                val imageBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("image", "pet_image.jpg", imageBody)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.updatePet(token, petId, dataBody, imagePart)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@EditPetActivity, "Pet updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        tvError.text = response.body()?.error?.message ?: "Failed to update pet."
                        tvError.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    tvError.text = "Connection error: ${e.message}"
                    tvError.visibility = View.VISIBLE
                }
            }
        }
    }
}