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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.features.auth.LandingActivity
import com.mobile.pawpal.features.auth.ProfileActivity
import com.mobile.pawpal.shared.AdoptionRequestItem
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
    private var myRequestMap = mapOf<Int, List<AdoptionRequestItem>>()
    private var currentFilter = "All"
    private lateinit var adapter: PetCardAdapter
    private var token = ""
    private lateinit var llPendingBanner: LinearLayout

    private lateinit var navBrowse: FrameLayout
    private lateinit var navRequests: FrameLayout
    private lateinit var navProfile: FrameLayout
    private lateinit var navIconBrowse: ImageView
    private lateinit var navIconRequests: ImageView
    private lateinit var navIconProfile: ImageView
    private lateinit var navLabelBrowse: TextView
    private lateinit var navLabelRequests: TextView
    private lateinit var navLabelProfile: TextView
    private lateinit var navBadgeRequests: TextView

    private var currentSection = "BROWSE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adopter_dashboard)

        tvName = findViewById(R.id.tvName)
        etSearch = findViewById(R.id.etSearch)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerView)
        llPendingBanner = findViewById(R.id.llPendingBanner)

        navBrowse = findViewById(R.id.navBrowse)
        navRequests = findViewById(R.id.navRequests)
        navProfile = findViewById(R.id.navProfile)
        navIconBrowse = findViewById(R.id.navIconBrowse)
        navIconRequests = findViewById(R.id.navIconRequests)
        navIconProfile = findViewById(R.id.navIconProfile)
        navLabelBrowse = findViewById(R.id.navLabelBrowse)
        navLabelRequests = findViewById(R.id.navLabelRequests)
        navLabelProfile = findViewById(R.id.navLabelProfile)
        navBadgeRequests = findViewById(R.id.navBadgeRequests)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = PetCardAdapter(listOf(), mapOf()) { pet ->
            val intent = Intent(this, PetDetailActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val filters = listOf("All", "Dogs", "Cats", "Birds", "Rabbits", "Others")
        val chipContainer = findViewById<LinearLayout>(R.id.chipContainer)
        filters.forEach { filter ->
            val chip = TextView(this).apply {
                text = filter
                textSize = 13f
                setPadding(32, 16, 32, 16)
                setTextColor(if (filter == "All") getColor(R.color.white) else getColor(R.color.green))
                background = if (filter == "All") getDrawable(R.drawable.chip_active) else getDrawable(R.drawable.chip_inactive)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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

        navBrowse.setOnClickListener { setNavActive("BROWSE") }
        navRequests.setOnClickListener {
            setNavActive("REQUESTS")
            startActivity(Intent(this, MyRequestsActivity::class.java))
        }
        navProfile.setOnClickListener {
            setNavActive("PROFILE")
            startActivity(Intent(this, ProfileActivity::class.java))
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
        tvName.text = "Hello, ${fullName.split(" ").first()} — browse pets available for adoption."
        setNavActive("BROWSE")
        loadPets()
    }

    private fun setNavActive(section: String) {
        currentSection = section
        val green = getColor(R.color.green)
        val muted = getColor(R.color.muted)

        navLabelBrowse.setTextColor(muted)
        navIconBrowse.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelRequests.setTextColor(muted)
        navIconRequests.setColorFilter(ContextCompat.getColor(this, R.color.muted))
        navLabelProfile.setTextColor(muted)
        navIconProfile.setColorFilter(ContextCompat.getColor(this, R.color.muted))

        when (section) {
            "BROWSE" -> {
                navLabelBrowse.setTextColor(green)
                navIconBrowse.setColorFilter(green)
            }
            "REQUESTS" -> {
                navLabelRequests.setTextColor(green)
                navIconRequests.setColorFilter(green)
            }
            "PROFILE" -> {
                navLabelProfile.setTextColor(green)
                navIconProfile.setColorFilter(green)
            }
        }
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
            val type = pet.type?.uppercase() ?: ""
            val matchesType = when (currentFilter) {
                "Dogs" -> type == "DOG"
                "Cats" -> type == "CAT"
                "Birds" -> type == "BIRD"
                "Rabbits" -> type == "RABBIT"
                "Others" -> !listOf("DOG", "CAT", "BIRD", "RABBIT").contains(type)
                else -> true
            }
            val matchesSearch = query.isEmpty() ||
                    pet.name.lowercase().contains(query) ||
                    pet.breed.lowercase().contains(query) ||
                    pet.location.lowercase().contains(query)
            matchesType && matchesSearch
        }
        adapter.updateList(filtered, myRequestMap)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadPets() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val petsResponse = RetrofitClient.instance.getAllPets(token)
                val requestsResponse = RetrofitClient.instance.getMyRequests(token)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (petsResponse.isSuccessful && petsResponse.body()?.success == true) {
                        allPets = petsResponse.body()?.data?.pets ?: listOf()
                        val requests = requestsResponse.body()?.data ?: emptyList()
                        myRequestMap = requests.groupBy { it.pet.id }

                        val pendingCount = requests.count { it.status.uppercase() == "PENDING" }
                        val hasPending = pendingCount > 0

                        llPendingBanner.visibility = if (hasPending) View.VISIBLE else View.GONE

                        if (pendingCount > 0) {
                            navBadgeRequests.visibility = View.VISIBLE
                            navBadgeRequests.text = if (pendingCount > 99) "99+" else pendingCount.toString()
                        } else {
                            navBadgeRequests.visibility = View.GONE
                        }

                        adapter.hasPendingRequest = hasPending
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
    private var myRequestMap: Map<Int, List<AdoptionRequestItem>>,
    private val onClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetCardAdapter.ViewHolder>() {

    var hasPendingRequest = false

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = pets[position]
        holder.name.text = pet.name
        holder.breed.text = pet.breed
        holder.age.text = "🕐 ${pet.age}"
        holder.type.text = "🐾 ${pet.type}"
        holder.location.text = "📍 ${pet.location}"

        val ctx = holder.itemView.context
        val requestsForPet = myRequestMap[pet.id]
        val latestStatus = requestsForPet?.maxByOrNull { it.id }?.status?.uppercase()
        val declineCount = requestsForPet?.count {
            it.status.uppercase() == "DECLINED" || it.status.uppercase() == "REJECTED"
        } ?: 0
        val permanentlyBlocked = declineCount >= 3

        when {
            permanentlyBlocked -> {
                holder.statusBadge.text = "Unavailable"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_declined)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.statusBadge.visibility = View.VISIBLE
                holder.btnAdopt.visibility = View.VISIBLE
                holder.btnAdopt.text = "✕  No Longer Available"
                holder.btnAdopt.setBackgroundResource(R.drawable.outline_button_bg)
                holder.btnAdopt.backgroundTintList = null
                holder.btnAdopt.setTextColor(ctx.getColor(R.color.dark))
                holder.btnAdopt.isEnabled = false
                holder.btnAdopt.setOnClickListener(null)
                holder.itemView.isClickable = false
                holder.itemView.setOnClickListener(null)
            }
            hasPendingRequest && latestStatus != "PENDING" -> {
                holder.statusBadge.text = "Available"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_available)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.statusBadge.visibility = View.VISIBLE
                holder.btnAdopt.visibility = View.VISIBLE
                holder.btnAdopt.text = "✕  Unavailable"
                holder.btnAdopt.setBackgroundResource(R.drawable.outline_button_bg)
                holder.btnAdopt.backgroundTintList = null
                holder.btnAdopt.setTextColor(ctx.getColor(R.color.muted))
                holder.btnAdopt.isEnabled = false
                holder.btnAdopt.setOnClickListener(null)
                holder.itemView.isClickable = false
                holder.itemView.setOnClickListener(null)
            }
            latestStatus == "PENDING" -> {
                holder.statusBadge.text = "Requested"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_pending)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.statusBadge.visibility = View.VISIBLE
                holder.btnAdopt.visibility = View.GONE
                holder.btnAdopt.setOnClickListener(null)
                holder.itemView.isClickable = true
                holder.itemView.setOnClickListener {
                    val intent = Intent(ctx, MyRequestsActivity::class.java)
                    ctx.startActivity(intent)
                }
            }
            latestStatus == "DECLINED" || latestStatus == "REJECTED" -> {
                holder.statusBadge.text = "Declined"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_declined)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.statusBadge.visibility = View.VISIBLE
                holder.btnAdopt.visibility = View.VISIBLE
                holder.btnAdopt.text = "↺  Try Again"
                holder.btnAdopt.setBackgroundResource(R.drawable.outline_button_bg)
                holder.btnAdopt.backgroundTintList = null
                holder.btnAdopt.setTextColor(ctx.getColor(R.color.dark))
                holder.btnAdopt.isEnabled = true
                holder.btnAdopt.setOnClickListener { onClick(pet) }
                holder.itemView.isClickable = true
                holder.itemView.setOnClickListener { onClick(pet) }
            }
            latestStatus == "APPROVED" -> {
                holder.statusBadge.text = "Approved"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_available)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.statusBadge.visibility = View.VISIBLE
                holder.btnAdopt.visibility = View.GONE
                holder.btnAdopt.setOnClickListener(null)
                holder.itemView.isClickable = true
                holder.itemView.setOnClickListener { onClick(pet) }
            }
            pet.status.uppercase() == "AVAILABLE" -> {
                holder.statusBadge.text = "Available"
                holder.statusBadge.setBackgroundResource(R.drawable.badge_available)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.statusBadge.visibility = View.VISIBLE
                holder.btnAdopt.visibility = View.VISIBLE
                holder.btnAdopt.text = "♡  Adopt Me"
                holder.btnAdopt.backgroundTintList = ctx.getColorStateList(R.color.orange)
                holder.btnAdopt.setTextColor(ctx.getColor(R.color.white))
                holder.btnAdopt.isEnabled = true
                holder.btnAdopt.setOnClickListener { onClick(pet) }
                holder.itemView.isClickable = true
                holder.itemView.setOnClickListener { onClick(pet) }
            }
            else -> {
                holder.statusBadge.text = pet.status
                holder.statusBadge.setBackgroundResource(R.drawable.badge_declined)
                holder.statusBadge.setTextColor(ctx.getColor(R.color.white))
                holder.statusBadge.visibility = View.VISIBLE
                holder.btnAdopt.visibility = View.GONE
                holder.btnAdopt.setOnClickListener(null)
                holder.itemView.isClickable = false
                holder.itemView.setOnClickListener(null)
            }
        }

        if (!pet.imageUrl.isNullOrEmpty()) {
            val fullUrl = if (pet.imageUrl.startsWith("http")) pet.imageUrl else "https://net-vanquish-poise.ngrok-free.dev${pet.imageUrl}"
            Glide.with(ctx).load(fullUrl).centerCrop().into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.pawlogo2)
        }
    }

    override fun getItemCount() = pets.size

    fun updateList(newList: List<Pet>, newRequestMap: Map<Int, List<AdoptionRequestItem>>) {
        pets = newList
        myRequestMap = newRequestMap
        notifyDataSetChanged()
    }
}