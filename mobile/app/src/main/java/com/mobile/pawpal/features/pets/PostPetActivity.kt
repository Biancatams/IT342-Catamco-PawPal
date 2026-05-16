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

class PostPetActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var spinnerBreed: Spinner
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerLocation: Spinner
    private lateinit var etAge: EditText
    private lateinit var etDescription: EditText
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

    private val traits = listOf(
        "Friendly", "Energetic", "Playful", "Good with Kids",
        "Good with Dogs", "Good with Cats", "House-trained", "Calm",
        "Loyal", "Curious", "Affectionate", "Independent"
    )

    private val petTypes = listOf("Dog", "Cat", "Bird", "Rabbit", "Other")
    private val genders = listOf("Unknown", "Male", "Female")

    private val breedsByType = mapOf(
        "Dog" to listOf("Unknown / Mixed", "Aspin (Mixed)", "Labrador Retriever", "German Shepherd",
            "Golden Retriever", "Shih Tzu", "Poodle", "Beagle", "Bulldog", "Chihuahua",
            "Pomeranian", "Dachshund", "Siberian Husky", "Other"),
        "Cat" to listOf("Unknown / Mixed", "Puspin (Mixed)", "Persian", "Siamese", "Maine Coon",
            "British Shorthair", "Ragdoll", "Bengal", "Abyssinian", "Other"),
        "Bird" to listOf("Unknown / Mixed", "Maya", "Budgerigar (Budgie)", "Cockatiel", "Lovebird",
            "African Grey", "Macaw", "Canary", "Other"),
        "Rabbit" to listOf("Unknown / Mixed", "Holland Lop", "Dutch", "Mini Rex", "Lionhead",
            "Angora", "Other"),
        "Other" to listOf("Unknown / Other")
    )

    private val locations = listOf(
        "Select your city/municipality",
        "Cebu City, Cebu", "Mandaue City, Cebu", "Lapu-Lapu City, Cebu",
        "Talisay City, Cebu", "Liloan, Cebu", "Consolacion, Cebu",
        "Minglanilla, Cebu", "Naga City, Cebu", "Toledo City, Cebu",
        "Danao City, Cebu", "Carcar City, Cebu", "Bogo City, Cebu",
        "Compostela, Cebu", "Cordova, Cebu", "Moalboal, Cebu",
        "Oslob, Cebu", "Alcoy, Cebu", "Dalaguete, Cebu",
        "San Fernando, Cebu", "Others"
    )

    private val IMAGE_PICK_CODE = 1001

    private val locationCoords = mapOf(
        "Cebu City, Cebu" to Pair(10.3157, 123.8854),
        "Mandaue City, Cebu" to Pair(10.3236, 123.9223),
        "Lapu-Lapu City, Cebu" to Pair(10.3103, 123.9494),
        "Talisay City, Cebu" to Pair(10.2442, 123.8484),
        "Liloan, Cebu" to Pair(10.3978, 123.9972),
        "Consolacion, Cebu" to Pair(10.3748, 123.9617),
        "Minglanilla, Cebu" to Pair(10.2442, 123.7967),
        "Naga City, Cebu" to Pair(10.2119, 123.7536),
        "Toledo City, Cebu" to Pair(10.3775, 123.6383),
        "Danao City, Cebu" to Pair(10.5228, 124.0264),
        "Carcar City, Cebu" to Pair(10.1063, 123.6411),
        "Bogo City, Cebu" to Pair(11.0517, 124.0053),
        "Compostela, Cebu" to Pair(10.4572, 124.0089),
        "Cordova, Cebu" to Pair(10.2667, 123.9500),
        "Moalboal, Cebu" to Pair(9.9383, 123.3961),
        "Oslob, Cebu" to Pair(9.5333, 123.4167),
        "Alcoy, Cebu" to Pair(9.7167, 123.5000),
        "Dalaguete, Cebu" to Pair(9.7614, 123.5342),
        "San Fernando, Cebu" to Pair(10.1667, 123.7167),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_pet)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("token", "")}"

        etName = findViewById(R.id.etName)
        spinnerType = findViewById(R.id.spinnerType)
        spinnerBreed = findViewById(R.id.spinnerBreed)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        etAge = findViewById(R.id.etAge)
        etDescription = findViewById(R.id.etDescription)
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
        spinnerLocation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        updateBreedSpinner("Dog")
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateBreedSpinner(petTypes[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        buildTraitChips()
        buildHealthCards()

        btnBack.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnSubmit.setOnClickListener { submitPet() }
    }

    private fun updateBreedSpinner(type: String) {
        val breeds = breedsByType[type] ?: listOf("Unknown / Mixed")
        spinnerBreed.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, breeds)
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
            rowLayout?.addView(chip)
        }
    }

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
            rowLayout?.addView(card)
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

    private fun submitPet() {
        val name = etName.text.toString().trim()
        val type = petTypes[spinnerType.selectedItemPosition]
        val breedList = breedsByType[type] ?: listOf("Unknown / Mixed")
        val breed = breedList[spinnerBreed.selectedItemPosition].let {
            if (it == "Unknown / Mixed" || it == "Unknown / Other") "" else it
        }
        val gender = genders[spinnerGender.selectedItemPosition]
        val locationIndex = spinnerLocation.selectedItemPosition
        val location = if (locationIndex == 0) "" else locations[locationIndex]
        val age = etAge.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (name.isEmpty() || age.isEmpty() || description.isEmpty() || locationIndex == 0) {
            tvError.text = "Please fill in all required fields."
            tvError.visibility = View.VISIBLE
            return
        }

        tvError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        val coords = locationCoords[location]
        val petData = mapOf(
            "name" to name,
            "type" to type.uppercase(),
            "breed" to breed,
            "gender" to if (gender == "Unknown") "" else gender.uppercase(),
            "age" to age,
            "description" to description,
            "location" to location,
            "latitude" to coords?.first,
            "longitude" to coords?.second,
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
                val response = RetrofitClient.instance.createPet(token, dataBody, imagePart)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSubmit.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@PostPetActivity, "Pet posted! Pending admin review.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        tvError.text = response.body()?.error?.message ?: "Failed to post pet."
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