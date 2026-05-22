package com.mobile.pawpal.features.adoption

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobile.pawpal.R
import com.mobile.pawpal.features.auth.ProfileActivity
import com.mobile.pawpal.shared.AdoptionRequestItem
import com.mobile.pawpal.shared.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyRequestsActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var tvPending: TextView
    private lateinit var tvApproved: TextView
    private lateinit var tvDeclined: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tabContainer: LinearLayout
    private var allRequests = listOf<AdoptionRequestItem>()
    private var currentFilter = "All"
    private lateinit var adapter: MyRequestsAdapter

    private lateinit var navBrowse: FrameLayout
    private lateinit var navRequests: FrameLayout
    private lateinit var navProfile: FrameLayout
    private lateinit var navIconBrowse: ImageView
    private lateinit var navIconRequests: ImageView
    private lateinit var navIconProfile: ImageView
    private lateinit var navLabelBrowse: TextView
    private lateinit var navLabelRequests: TextView
    private lateinit var navLabelProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_requests)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"

        tvTotal = findViewById(R.id.tvTotal)
        tvPending = findViewById(R.id.tvPending)
        tvApproved = findViewById(R.id.tvApproved)
        tvDeclined = findViewById(R.id.tvDeclined)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        tabContainer = findViewById(R.id.tabContainer)

        navBrowse = findViewById(R.id.navBrowse)
        navRequests = findViewById(R.id.navRequests)
        navProfile = findViewById(R.id.navProfile)
        navIconBrowse = findViewById(R.id.navIconBrowse)
        navIconRequests = findViewById(R.id.navIconRequests)
        navIconProfile = findViewById(R.id.navIconProfile)
        navLabelBrowse = findViewById(R.id.navLabelBrowse)
        navLabelRequests = findViewById(R.id.navLabelRequests)
        navLabelProfile = findViewById(R.id.navLabelProfile)

        setNavActive("REQUESTS")

        navBrowse.setOnClickListener {
            val intent = Intent(this, AdopterDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        navRequests.setOnClickListener { }
        navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyRequestsAdapter(listOf()) { request ->
            if (request.status.uppercase() == "APPROVED") {
                val intent = Intent(this, RequestAcceptedActivity::class.java)
                intent.putExtra("requestId", request.id)
                intent.putExtra("petName", request.pet.name)
                intent.putExtra("petBreed", request.pet.breed)
                intent.putExtra("petAge", request.pet.age)
                intent.putExtra("petImageUrl", request.pet.imageUrl ?: "")
                intent.putExtra("ownerName", request.owner.fullName)
                intent.putExtra("ownerPhone", request.owner.phoneNumber ?: "")
                intent.putExtra("ownerEmail", request.owner.email)
                intent.putExtra("ownerImageUrl", request.owner.profileImageUrl ?: "")
                startActivity(intent)
            } else {
                val intent = Intent(this, PetDetailActivity::class.java)
                intent.putExtra("petId", request.pet.id)
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter

        val filters = listOf("All", "Pending", "Approved", "Declined")
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
                updateTabs(filter)
                applyFilter()
            }
            tabContainer.addView(tab)
        }

        loadRequests(token)
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
            "Pending" -> allRequests.filter { it.status.uppercase() == "PENDING" }
            "Approved" -> allRequests.filter { it.status.uppercase() == "APPROVED" }
            "Declined" -> allRequests.filter { it.status.uppercase() == "DECLINED" }
            else -> allRequests
        }
        adapter.updateList(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadRequests(token: String) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getMyRequests(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        allRequests = response.body()?.data ?: listOf()
                        tvTotal.text = allRequests.size.toString()
                        tvPending.text = allRequests.count { it.status.uppercase() == "PENDING" }.toString()
                        tvApproved.text = allRequests.count { it.status.uppercase() == "APPROVED" }.toString()
                        tvDeclined.text = allRequests.count { it.status.uppercase() == "DECLINED" }.toString()
                        applyFilter()
                    } else {
                        tvEmpty.text = "Failed to load requests."
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

    class MyRequestsAdapter(
        private var requests: List<AdoptionRequestItem>,
        private val onClick: (AdoptionRequestItem) -> Unit
    ) : RecyclerView.Adapter<MyRequestsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPet: ImageView = view.findViewById(R.id.ivPetImage)
            val tvPetName: TextView = view.findViewById(R.id.tvPetName)
            val tvBreed: TextView = view.findViewById(R.id.tvBreed)
            val tvAge: TextView = view.findViewById(R.id.tvAge)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val tvMessage: TextView = view.findViewById(R.id.tvMessage)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_my_request, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val req = requests[position]
            val ctx = holder.itemView.context

            holder.tvPetName.text = req.pet.name
            holder.tvBreed.text = req.pet.breed
            holder.tvAge.text = "🕐 ${req.pet.age}"
            holder.tvStatus.text = req.status
            holder.tvDate.text = "Submitted ${formatDate(req.createdAt)}"

            when (req.status.uppercase()) {
                "APPROVED" -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.badge_available)
                    holder.tvStatus.setTextColor(ctx.getColor(R.color.white))
                    holder.tvMessage.text = "✓  Your adoption request has been approved! Contact the owner."
                    holder.tvMessage.visibility = View.VISIBLE
                }
                "PENDING" -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.badge_pending)
                    holder.tvStatus.setTextColor(ctx.getColor(R.color.white))
                    holder.tvMessage.text = "⏳  Your request is pending review."
                    holder.tvMessage.visibility = View.VISIBLE
                }
                "DECLINED" -> {
                    holder.tvStatus.setBackgroundResource(R.drawable.badge_declined)
                    holder.tvStatus.setTextColor(ctx.getColor(R.color.white))
                    val reason = req.declineReason
                    holder.tvMessage.text = if (!reason.isNullOrBlank())
                        "✕  Declined: $reason"
                    else
                        "✕  Your request was declined."
                    holder.tvMessage.visibility = View.VISIBLE
                }
            }

            if (!req.pet.imageUrl.isNullOrEmpty()) {
                val fullUrl = if (req.pet.imageUrl.startsWith("http")) req.pet.imageUrl
                else "https://net-vanquish-poise.ngrok-free.dev${req.pet.imageUrl}"
                Glide.with(ctx).load(fullUrl).centerCrop().into(holder.ivPet)
            } else {
                holder.ivPet.setImageResource(R.drawable.pawlogo2)
            }

            holder.btnViewDetails.setOnClickListener { onClick(req) }
            holder.itemView.setOnClickListener { onClick(req) }
        }

        override fun getItemCount() = requests.size

        fun updateList(newList: List<AdoptionRequestItem>) {
            requests = newList
            notifyDataSetChanged()
        }

        private fun formatDate(dateStr: String): String {
            return try {
                val parts = dateStr.substring(0, 10).split("-")
                val months = listOf("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                "${months[parts[1].toInt()]} ${parts[2]}, ${parts[0]}"
            } catch (e: Exception) { dateStr }
        }
    }
}