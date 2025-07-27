// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/PurchaseHistoryActivity.kt
package com.ahmar.inventorytokokotlinumb

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmar.inventorytokokotlinumb.adapters.PurchaseHistoryAdapter
import com.ahmar.inventorytokokotlinumb.viewmodels.PurchaseHistoryViewModel
import com.ahmar.inventorytokokotlinumb.models.Purchase // Pastikan ini diimpor dengan package yang benar

class PurchaseHistoryActivity : AppCompatActivity() {

    private lateinit var purchasesRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyHistoryTextView: TextView

    private lateinit var purchaseHistoryAdapter: PurchaseHistoryAdapter
    private val purchaseHistoryViewModel: PurchaseHistoryViewModel by viewModels()

    private var currentUserRole: String = "customer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_history)

        // Setup ActionBar
        supportActionBar?.title = "Riwayat Pembelian"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tombol kembali

        // Inisialisasi View
        purchasesRecyclerView = findViewById(R.id.purchasesRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyHistoryTextView = findViewById(R.id.emptyHistoryTextView)

        // Dapatkan peran pengguna dari SharedPreferences
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        currentUserRole = sharedPref.getString("user_role", "customer") ?: "customer"

        // Setup RecyclerView
        purchasesRecyclerView.layoutManager = LinearLayoutManager(this)
        purchaseHistoryAdapter = PurchaseHistoryAdapter(mutableListOf())
        purchasesRecyclerView.adapter = purchaseHistoryAdapter

        // Observasi LiveData dari ViewModel
        // Explicitly specify the type of 'purchases' parameter
        purchaseHistoryViewModel.purchases.observe(this) { purchases: List<Purchase>? ->
            purchases?.let {
                purchaseHistoryAdapter.updatePurchases(it)
                emptyHistoryTextView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        purchaseHistoryViewModel.isLoading.observe(this) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        purchaseHistoryViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                purchaseHistoryViewModel.clearErrorMessage()
            }
        }

        // Muat riwayat pembelian berdasarkan peran pengguna
        loadPurchaseHistory()
    }

    // Handle tombol kembali di ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadPurchaseHistory() {
        if (currentUserRole == "admin") {
            purchaseHistoryViewModel.loadAdminPurchases()
        } else {
            // Untuk customer, kita tidak perlu userId di sini karena API sudah menggunakan token
            purchaseHistoryViewModel.loadCustomerPurchases()
        }
    }
}
