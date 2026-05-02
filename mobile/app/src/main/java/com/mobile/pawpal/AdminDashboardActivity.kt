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

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val prefs = getSharedPreferences("pawpal_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"

        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }

        loadPendingPets(token)
    }

    private fun loadPendingPets(token: String) {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getUnderReviewPets(token)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body()?.success == true) {
                        val pets = response.body()?.data?.pets ?: listOf()
                        if (pets.isEmpty()) {
                            tvEmpty.text = "No pets pending review."
                            tvEmpty.visibility = View.VISIBLE
                        } else {
                            recyclerView.adapter = AdminPetAdapter(pets, token)
                        }
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

class AdminPetAdapter(
    private val pets: List<Pet>,
    private val token: String
) : RecyclerView.Adapter<AdminPetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivPetImage)
        val name: TextView = view.findViewById(R.id.tvPetName)
        val breed: TextView = view.findViewById(R.id.tvBreed)
        val location: TextView = view.findViewById(R.id.tvLocation)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_pet_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = pets[position]
        holder.name.text = pet.name
        holder.breed.text = pet.breed
        holder.location.text = "📍 ${pet.location}"

        if (!pet.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context).load(pet.imageUrl).centerCrop().into(holder.image)
        }

        holder.btnApprove.setOnClickListener {
            approvePet(pet.id, token, holder.itemView.context)
        }
        holder.btnReject.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Reject - coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = pets.size

    private fun approvePet(petId: Int, token: String, context: android.content.Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.approvePet(token, petId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Pet approved!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}