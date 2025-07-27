// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/MainActivity.kt
package com.ahmar.inventorytokokotlinumb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmar.inventorytokokotlinumb.adapters.ProductAdapter
import com.ahmar.inventorytokokotlinumb.models.Product
import com.ahmar.inventorytokokotlinumb.network.RetrofitClient
import com.ahmar.inventorytokokotlinumb.viewmodels.ProductViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var userRoleTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var purchasesHistoryButton: Button
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var fabCart: FloatingActionButton
    private lateinit var userManagementButton: Button // Tombol baru

    private lateinit var productAdapter: ProductAdapter
    private val productViewModel: ProductViewModel by viewModels()

    private var currentUserRole: String = "customer"
    private var currentUserId: Int = -1

    private val TAG = "MainActivityDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi View
        welcomeTextView = findViewById(R.id.welcomeTextView)
        userRoleTextView = findViewById(R.id.userRoleTextView)
        logoutButton = findViewById(R.id.logoutButton)
        purchasesHistoryButton = findViewById(R.id.purchasesHistoryButton)
        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        fabCart = findViewById(R.id.fabCart)
        userManagementButton = findViewById(R.id.userManagementButton) // Inisialisasi tombol baru

        // Dapatkan peran pengguna dari SharedPreferences
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Pengguna")
        currentUserRole = sharedPref.getString("user_role", "customer") ?: "customer"
        currentUserId = sharedPref.getInt("user_id", -1)

        // PENTING: Set ulang token di RetrofitClient saat MainActivity dibuat
        val storedAuthToken = sharedPref.getString("auth_token", null)
        if (storedAuthToken != null) {
            RetrofitClient.setAuthToken(storedAuthToken)
            Log.d(TAG, "onCreate: RetrofitClient authToken set from SharedPreferences: $storedAuthToken")
        } else {
            Log.w(TAG, "onCreate: No auth token found in SharedPreferences for MainActivity. Redirecting to LoginActivity.")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        welcomeTextView.text = "Selamat Datang, $username!"
        userRoleTextView.text = "Anda login sebagai: $currentUserRole"

        // Setup RecyclerView
        productsRecyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            userRole = currentUserRole,
            onAddToCartClick = { product ->
                if (currentUserRole == "customer") {
                    showAddToCartDialog(product)
                }
            },
            onEditClick = { product ->
                if (currentUserRole == "admin") {
                    showEditProductDialog(product)
                }
            },
            onDeleteClick = { product ->
                if (currentUserRole == "admin") {
                    showDeleteProductConfirmation(product)
                }
            }
        )
        productsRecyclerView.adapter = productAdapter

        // Observasi LiveData dari ViewModel
        productViewModel.products.observe(this) { products ->
            products?.let {
                productAdapter.updateProducts(it)
            }
        }

        productViewModel.isLoading.observe(this) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        productViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                productViewModel.clearErrorMessage()
            }
        }

        // Muat produk saat Activity dibuat
        productViewModel.loadProducts(currentUserRole)

        // Atur FAB berdasarkan peran
        setupFloatingActionButton(currentUserRole)

        logoutButton.setOnClickListener {
            performLogout()
        }

        // Tampilkan/sembunyikan tombol riwayat pembelian berdasarkan peran
        if (currentUserRole == "customer") {
            purchasesHistoryButton.visibility = View.VISIBLE
            purchasesHistoryButton.setOnClickListener {
                val intent = Intent(this, PurchaseHistoryActivity::class.java)
                startActivity(intent)
            }
            userManagementButton.visibility = View.GONE // Sembunyikan untuk customer
        } else if (currentUserRole == "admin") {
            purchasesHistoryButton.visibility = View.VISIBLE // Admin juga bisa lihat riwayat pembelian
            purchasesHistoryButton.setOnClickListener {
                val intent = Intent(this, PurchaseHistoryActivity::class.java)
                startActivity(intent)
            }
            userManagementButton.visibility = View.VISIBLE // Tampilkan untuk admin
            userManagementButton.setOnClickListener {
                val intent = Intent(this, UserManagementActivity::class.java)
                startActivity(intent)
            }
        } else {
            purchasesHistoryButton.visibility = View.GONE
            userManagementButton.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        productViewModel.loadProducts(currentUserRole)
    }

    private fun setupFloatingActionButton(role: String) {
        if (role == "customer") {
            fabCart.setImageResource(R.drawable.ic_shopping_cart)
            fabCart.setOnClickListener {
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            }
        } else if (role == "admin") {
            fabCart.setImageResource(R.drawable.ic_add_product)
            fabCart.setOnClickListener {
                showAddProductDialog()
            }
        }
    }

    private fun showAddToCartDialog(product: Product) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tambah ke Keranjang")
        builder.setMessage("Masukkan jumlah ${product.name} yang ingin ditambahkan (Stok tersedia: ${product.stock}):")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText("1")
        builder.setView(input)

        builder.setPositiveButton("Tambah") { dialog, _ ->
            val quantityStr = input.text.toString()
            if (quantityStr.isNotEmpty()) {
                val quantity = quantityStr.toInt()
                if (quantity > 0 && quantity <= product.stock) {
                    productViewModel.addProductToCart(product.id, quantity)
                } else {
                    Toast.makeText(this, "Kuantitas tidak valid atau melebihi stok!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kuantitas tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showAddProductDialog() {
        val dialog = AddProductDialogFragment()
        dialog.show(supportFragmentManager, "AddProductDialog")
    }

    private fun showEditProductDialog(product: Product) {
        val dialog = EditProductDialogFragment.newInstance(product)
        dialog.show(supportFragmentManager, "EditProductDialog")
    }

    private fun showDeleteProductConfirmation(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus produk '${product.name}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                productViewModel.deleteProduct(product.id, product.name)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun performLogout() {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("auth_token")
            remove("user_role")
            remove("user_id")
            remove("username")
            apply()
        }

        RetrofitClient.setAuthToken(null)

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
