package com.mobile.pawpal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotalPets: TextView
    private lateinit var tvAvailable: TextView
    private lateinit var tvPendingRequests: TextView
    private lateinit var tvSubtitle: TextView
    private var allPets = listOf<Pet>()
    private var currentFilter = "All"
    private lateinit var adapter: OwnerPetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "User") ?: "User"
        val token = "Bearer ${prefs.getString("token", "")}"

        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvTotalPets = findViewById(R.id.tvTotalPets)
        tvAvailable = findViewById(R.id.tvAvailable)
        tvPendingRequests = findViewById(R.id.tvPendingRequests)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerView)

        tvSubtitle.text = "Hello, ${fullName.split(" ").first()} — manage your pet listings and adoption requests."

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OwnerPetAdapter(listOf(),
            onViewRequests = { pet ->
                // TODO: View requests screen
                Toast.makeText(this, "Requests for ${pet.name} - coming soon!", Toast.LENGTH_SHORT).show()
            },
            onDelete = { pet ->
                // TODO: Delete pet
                Toast.makeText(this, "Delete ${pet.name} - coming soon!", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        // Filter tabs
        val filters = listOf("All", "Pending Review", "Available", "Rejected", "Adopted")
        val tabContainer = findViewById<LinearLayout>(R.id.tabContainer)
        filters.forEach { filter ->
            val tab = TextView(this).apply {
                text = filter
                textSize = 13f
                setPadding(32, 16, 32, 16)
                setTextColor(if (filter == "All") getColor(R.color.white) else getColor(R.color.green))
                background = if (filter == "All") getDrawable(R.drawable.chip_active) else getDrawable(R.drawable.chip_inactive)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = 8
                layoutParams = lp
            }
            tab.setOnClickListener {
                currentFilter = filter
                updateTabs(tabContainer, filters, filter)
                applyFilter()
            }
            tabContainer.addView(tab)
        }

        findViewById<TextView>(R.id.btnProfile).setOnClickListener {
            Toast.makeText(this, "Profile - coming soon!", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnPostPet).setOnClickListener {
            Toast.makeText(this, "Post a Pet - coming soon!", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }

        loadMyPets(token)
    }

    private fun updateTabs(container: LinearLayout, filters: List<String>, active: String) {
        for (i in 0 until container.childCount) {
            val tab = container.getChildAt(i) as TextView
            val isActive = tab.text == active
            tab.setTextColor(if (isActive) getColor(R.color.white) else getColor(R.color.green))
            tab.background = if (isActive) getDrawable(R.drawable.chip_active) else getDrawable(R.drawable.chip_inactive)
        }
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            "Pending Review" -> allPets.filter { it.status.uppercase() == "UNDER_REVIEW" }
            "Available" -> allPets.filter { it.status.uppercase() == "AVAILABLE" }
            "Rejected" -> allPets.filter { it.status.uppercase() == "REJECTED" }
            "Adopted" -> allPets.filter { it.status.uppercase() == "ADOPTED" }
            else -> allPets
        }
        adapter.updateList(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadMyPets(token: String) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getMyPets(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        allPets = response.body()?.data?.pets ?: listOf()
                        tvTotalPets.text = allPets.size.toString()
                        tvAvailable.text = allPets.count { it.status.uppercase() == "AVAILABLE" }.toString()
                        tvPendingRequests.text = "0"
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

class OwnerPetAdapter(
    private var pets: List<Pet>,
    private val onViewRequests: (Pet) -> Unit,
    private val onDelete: (Pet) -> Unit
) : RecyclerView.Adapter<OwnerPetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivPetImage)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val name: TextView = view.findViewById(R.id.tvPetName)
        val breed: TextView = view.findViewById(R.id.tvBreed)
        val age: TextView = view.findViewById(R.id.tvAge)
        val type: TextView = view.findViewById(R.id.tvType)
        val location: TextView = view.findViewById(R.id.tvLocation)
        val btnViewRequests: Button = view.findViewById(R.id.btnViewRequests)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_owner_pet_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = pets[position]
        holder.name.text = pet.name
        holder.breed.text = pet.breed
        holder.age.text = "🕐 ${pet.age}"
        holder.type.text = "🐾 ${pet.petType}"
        holder.location.text = "📍 ${pet.location}"
        holder.status.text = pet.status

        val ctx = holder.itemView.context
        when (pet.status.uppercase()) {
            "AVAILABLE" -> holder.status.setBackgroundResource(R.drawable.badge_available)
            "ADOPTED" -> holder.status.setBackgroundResource(R.drawable.badge_adopted)
            "UNDER_REVIEW" -> holder.status.setBackgroundResource(R.drawable.badge_pending)
            "REJECTED" -> holder.status.setBackgroundResource(R.drawable.badge_declined)
            else -> holder.status.setBackgroundResource(R.drawable.badge_available)
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

        holder.btnViewRequests.setOnClickListener { onViewRequests(pet) }
        holder.btnDelete.setOnClickListener { onDelete(pet) }
    }

    override fun getItemCount() = pets.size

    fun updateList(newList: List<Pet>) {
        pets = newList
        notifyDataSetChanged()
    }
}