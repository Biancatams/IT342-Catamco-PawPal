package com.mobile.pawpal.features.adoption

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.features.auth.LandingActivity
import com.mobile.pawpal.shared.Pet
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdopterDashboardActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var etSearch: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private var allPets = listOf<Pet>()
    private var currentFilter = "All"
    private lateinit var adapter: PetCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adopter_dashboard)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "User") ?: "User"
        val token = "Bearer ${prefs.getString("token", "")}"

        tvName = findViewById(R.id.tvName)
        etSearch = findViewById(R.id.etSearch)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerView)

        tvName.text = "Hello, ${fullName.split(" ").first()} — browse pets available for adoption."

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = PetCardAdapter(listOf()) { pet ->
            // TODO: open pet detail
        }
        recyclerView.adapter = adapter

        // Filter chips
        val filters = listOf("All", "Dogs", "Cats", "Birds", "Rabbits", "Others")
        val chipContainer = findViewById<LinearLayout>(R.id.chipContainer)
        filters.forEach { filter ->
            val chip = TextView(this).apply {
                text = filter
                textSize = 13f
                setPadding(32, 16, 32, 16)
                setTextColor(if (filter == "All") getColor(R.color.white) else getColor(R.color.green))
                background = if (filter == "All")
                    getDrawable(R.drawable.chip_active)
                else
                    getDrawable(R.drawable.chip_inactive)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = 8
                layoutParams = lp
            }
            chip.setOnClickListener {
                currentFilter = filter
                updateChips(chipContainer, filters, filter)
                applyFilter()
            }
            chipContainer.addView(chip)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilter() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Nav buttons
        findViewById<TextView>(R.id.btnMyRequests).setOnClickListener {
            // TODO: My Requests screen
        }
        findViewById<TextView>(R.id.btnProfile).setOnClickListener {
            // TODO: Profile screen
        }
        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }

        loadPets(token)
    }

    private fun updateChips(container: LinearLayout, filters: List<String>, active: String) {
        for (i in 0 until container.childCount) {
            val chip = container.getChildAt(i) as TextView
            val isActive = chip.text == active
            chip.setTextColor(if (isActive) getColor(R.color.white) else getColor(R.color.green))
            chip.background = if (isActive) getDrawable(R.drawable.chip_active) else getDrawable(R.drawable.chip_inactive)
        }
    }

    private fun applyFilter() {
        val query = etSearch.text.toString().lowercase()
        val filtered = allPets.filter { pet ->
            val matchesType = when (currentFilter) {
                "Dogs" -> pet.petType.uppercase() == "DOG"
                "Cats" -> pet.petType.uppercase() == "CAT"
                "Birds" -> pet.petType.uppercase() == "BIRD"
                "Rabbits" -> pet.petType.uppercase() == "RABBIT"
                "Others" -> !listOf("DOG","CAT","BIRD","RABBIT").contains(pet.petType.uppercase())
                else -> true
            }
            val matchesSearch = query.isEmpty() ||
                    pet.name.lowercase().contains(query) ||
                    pet.breed.lowercase().contains(query) ||
                    pet.location.lowercase().contains(query)
            matchesType && matchesSearch
        }
        adapter.updateList(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadPets(token: String) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllPets(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        allPets = response.body()?.data?.pets ?: listOf()
                        applyFilter()
                    } else {
                        tvEmpty.text = "Failed to load pets."
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
}

class PetCardAdapter(
    private var pets: List<Pet>,
    private val onClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivPetImage)
        val statusBadge: TextView = view.findViewById(R.id.tvStatus)
        val name: TextView = view.findViewById(R.id.tvPetName)
        val breed: TextView = view.findViewById(R.id.tvBreed)
        val age: TextView = view.findViewById(R.id.tvAge)
        val type: TextView = view.findViewById(R.id.tvType)
        val location: TextView = view.findViewById(R.id.tvLocation)
        val btnAdopt: Button = view.findViewById(R.id.btnAdopt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = pets[position]
        holder.name.text = pet.name
        holder.breed.text = pet.breed
        holder.age.text = "🕐 ${pet.age}"
        holder.type.text = "🐾 ${pet.petType}"
        holder.location.text = "📍 ${pet.location}"
        holder.statusBadge.text = pet.status
        holder.statusBadge.visibility = View.VISIBLE

        val ctx = holder.itemView.context
        when (pet.status.uppercase()) {
            "AVAILABLE" -> {
                holder.statusBadge.setBackgroundResource(R.drawable.badge_available)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.btnAdopt.visibility = View.VISIBLE
                holder.btnAdopt.text = "♡  Adopt Me"
            }
            else -> {
                holder.statusBadge.setBackgroundResource(R.drawable.badge_declined)
                holder.btnAdopt.visibility = View.GONE
            }
        }

        if (!pet.imageUrl.isNullOrEmpty()) {
            val fullUrl = if (pet.imageUrl.startsWith("http")) {
                pet.imageUrl
            } else {
                "http://10.0.2.2:8080${pet.imageUrl}"
            }
            Glide.with(ctx).load(fullUrl).centerCrop().into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.pawlogo)
        }

        holder.btnAdopt.setOnClickListener { onClick(pet) }
        holder.itemView.setOnClickListener { onClick(pet) }
    }

    override fun getItemCount() = pets.size

    fun updateList(newList: List<Pet>) {
        pets = newList
        notifyDataSetChanged()
    }
}