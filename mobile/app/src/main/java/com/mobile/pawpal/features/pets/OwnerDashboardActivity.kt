package com.mobile.pawpal.features.pets

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
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

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvTotalPets: TextView
    private lateinit var tvAvailable: TextView
    private lateinit var tvPendingRequests: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tabContainer: LinearLayout
    private var allPets = listOf<Pet>()
    private var currentFilter = "All"
    private lateinit var adapter: OwnerPetAdapter
    private var token = ""

    private lateinit var navMyPets: FrameLayout
    private lateinit var navPostPet: FrameLayout
    private lateinit var navOwnerProfile: FrameLayout
    private lateinit var navIconMyPets: ImageView
    private lateinit var navIconPostPet: ImageView
    private lateinit var navIconOwnerProfile: ImageView
    private lateinit var navLabelMyPets: TextView
    private lateinit var navLabelPostPet: TextView
    private lateinit var navLabelOwnerProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvTotalPets = findViewById(R.id.tvTotalPets)
        tvAvailable = findViewById(R.id.tvAvailable)
        tvPendingRequests = findViewById(R.id.tvPendingRequests)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerView)
        tabContainer = findViewById(R.id.tabContainer)

        navMyPets = findViewById(R.id.navMyPets)
        navPostPet = findViewById(R.id.navPostPet)
        navOwnerProfile = findViewById(R.id.navOwnerProfile)
        navIconMyPets = findViewById(R.id.navIconMyPets)
        navIconPostPet = findViewById(R.id.navIconPostPet)
        navIconOwnerProfile = findViewById(R.id.navIconOwnerProfile)
        navLabelMyPets = findViewById(R.id.navLabelMyPets)
        navLabelPostPet = findViewById(R.id.navLabelPostPet)
        navLabelOwnerProfile = findViewById(R.id.navLabelOwnerProfile)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OwnerPetAdapter(
            listOf(),
            onViewRequests = { pet ->
                val intent = Intent(this, ViewRequestsActivity::class.java)
                intent.putExtra("petId", pet.id)
                intent.putExtra("petName", pet.name)
                startActivity(intent)
            },
            onEdit = { pet ->
                val intent = Intent(this, EditPetActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            },
            onDelete = { pet -> showDeleteDialog(pet) }
        )
        recyclerView.adapter = adapter

        val filters = listOf("All", "Pending Review", "Available", "Rejected", "Adopted")
        filters.forEach { filter ->
            val tab = TextView(this).apply {
                text = filter
                textSize = 13f
                setPadding(32, 16, 32, 16)
                setTextColor(if (filter == "All") getColor(R.color.white) else getColor(R.color.green))
                background = if (filter == "All") getDrawable(R.drawable.chip_active) else getDrawable(R.drawable.chip_inactive)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.marginEnd = 8
                layoutParams = lp
            }
            tab.setOnClickListener {
                currentFilter = filter
                updateTabs(filter)
                applyFilter()
            }
            tabContainer.addView(tab)
        }

        navMyPets.setOnClickListener { setNavActive("MY_PETS") }
        navPostPet.setOnClickListener {
            setNavActive("POST")
            startActivity(Intent(this, PostPetActivity::class.java))
        }
        navOwnerProfile.setOnClickListener {
            setNavActive("PROFILE")
            startActivity(Intent(this, OwnerProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            getSharedPreferences("pawpal_prefs", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LandingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "User") ?: "User"
        token = "Bearer ${prefs.getString("token", "")}"
        tvSubtitle.text = "Hello, ${fullName.split(" ").first()} — manage your pet listings."
        setNavActive("MY_PETS")
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
            "MY_PETS" -> {
                navLabelMyPets.setTextColor(green)
                navIconMyPets.setColorFilter(green)
            }
            "POST" -> {
                navLabelPostPet.setTextColor(green)
                navIconPostPet.setColorFilter(green)
            }
            "PROFILE" -> {
                navLabelOwnerProfile.setTextColor(green)
                navIconOwnerProfile.setColorFilter(green)
            }
        }
    }

    private fun showDeleteDialog(pet: Pet) {
        AlertDialog.Builder(this)
            .setTitle("Delete Listing?")
            .setMessage("Are you sure you want to delete ${pet.name}'s listing? This action cannot be undone.")
            .setPositiveButton("Yes, Delete") { _, _ -> deletePet(pet) }
            .setNegativeButton("No, Keep It", null)
            .show()
    }

    private fun deletePet(pet: Pet) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.deletePet(token, pet.id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@OwnerDashboardActivity, "${pet.name} deleted.", Toast.LENGTH_SHORT).show()
                        loadMyPets()
                    } else {
                        Toast.makeText(this@OwnerDashboardActivity, "Failed to delete.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@OwnerDashboardActivity, "Connection error.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTabs(active: String) {
        for (i in 0 until tabContainer.childCount) {
            val tab = tabContainer.getChildAt(i) as TextView
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

    private fun loadMyPets() {
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
                        val pending = allPets.sumOf { it.requestCount ?: 0 }
                        tvPendingRequests.text = pending.toString()
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
    private val onEdit: (Pet) -> Unit,
    private val onDelete: (Pet) -> Unit
) : RecyclerView.Adapter<OwnerPetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivPetImage)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val name: TextView = view.findViewById(R.id.tvPetName)
        val breed: TextView = view.findViewById(R.id.tvBreed)
        val age: TextView = view.findViewById(R.id.tvAge)
        val type: TextView = view.findViewById(R.id.tvType)
        val location: TextView = view.findViewById(R.id.tvLocation)
        val tvRequestCount: TextView = view.findViewById(R.id.tvRequestCount)
        val btnViewRequests: Button = view.findViewById(R.id.btnViewRequests)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val llAdopted: LinearLayout = view.findViewById(R.id.llAdopted)
        val llActions: LinearLayout = view.findViewById(R.id.llActions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_owner_pet_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = pets[position]
        val ctx = holder.itemView.context

        holder.name.text = pet.name
        holder.breed.text = pet.breed
        holder.age.text = pet.age
        holder.type.text = pet.type ?: ""
        holder.location.text = pet.location
        holder.status.text = pet.status.replace("_", " ")

        val count = pet.requestCount ?: 0
        holder.tvRequestCount.text = if (count == 0) "No pending requests" else "$count pending request(s)"

        when (pet.status.uppercase()) {
            "AVAILABLE" -> {
                holder.status.setBackgroundResource(R.drawable.badge_available)
                holder.status.setTextColor(ctx.getColor(R.color.white))
                holder.llAdopted.visibility = View.GONE
                holder.llActions.visibility = View.VISIBLE
                holder.btnDelete.visibility = View.VISIBLE
            }
            "UNDER_REVIEW" -> {
                holder.status.setBackgroundResource(R.drawable.badge_pending)
                holder.status.setTextColor(ctx.getColor(R.color.white))
                holder.llAdopted.visibility = View.GONE
                holder.llActions.visibility = View.VISIBLE
                holder.btnDelete.visibility = View.VISIBLE
            }
            "REJECTED" -> {
                holder.status.setBackgroundResource(R.drawable.badge_declined)
                holder.status.setTextColor(ctx.getColor(R.color.white))
                holder.llAdopted.visibility = View.GONE
                holder.llActions.visibility = View.VISIBLE
                holder.btnDelete.visibility = View.VISIBLE
            }
            "ADOPTED" -> {
                holder.status.setBackgroundResource(R.drawable.badge_available)
                holder.status.setTextColor(ctx.getColor(R.color.white))
                holder.llAdopted.visibility = View.VISIBLE
                holder.llActions.visibility = View.GONE
                holder.btnDelete.visibility = View.GONE
            }
            else -> {
                holder.llAdopted.visibility = View.GONE
                holder.llActions.visibility = View.VISIBLE
                holder.btnDelete.visibility = View.VISIBLE
            }
        }

        if (!pet.imageUrl.isNullOrEmpty()) {
            val fullUrl = if (pet.imageUrl.startsWith("http")) pet.imageUrl else "https://net-vanquish-poise.ngrok-free.dev${pet.imageUrl}"
            Glide.with(ctx).load(fullUrl).centerCrop().into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.pawlogo2)
        }

        holder.btnViewRequests.setOnClickListener { onViewRequests(pet) }
        holder.btnEdit.setOnClickListener { onEdit(pet) }
        holder.btnDelete.setOnClickListener { onDelete(pet) }
    }

    override fun getItemCount() = pets.size

    fun updateList(newList: List<Pet>) {
        pets = newList
        notifyDataSetChanged()
    }
}