// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/PurchaseHistoryActivity.kt
package com.warni.inventorytokokotlinumb

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
import com.warni.inventorytokokotlinumb.adapters.PurchaseHistoryAdapter
import com.warni.inventorytokokotlinumb.viewmodels.PurchaseHistoryViewModel
import com.warni.inventorytokokotlinumb.models.Purchase

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
        // Dapatkan peran pengguna dari SharedPreferences terlebih dahulu
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        currentUserRole = sharedPref.getString("user_role", "customer") ?: "customer"

        // Atur judul ActionBar berdasarkan peran
        supportActionBar?.title = if (currentUserRole == "admin") {
            "Produk Terjual"
        } else {
            "Riwayat Pembelian"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tombol kembali


        // Inisialisasi View
        purchasesRecyclerView = findViewById(R.id.purchasesRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyHistoryTextView = findViewById(R.id.emptyHistoryTextView)

        // Setup RecyclerView
        purchasesRecyclerView.layoutManager = LinearLayoutManager(this) // Mengoreksi nama variabel
        purchaseHistoryAdapter = PurchaseHistoryAdapter(mutableListOf())
        purchasesRecyclerView.adapter = purchaseHistoryAdapter

        // Observasi LiveData dari ViewModel
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
