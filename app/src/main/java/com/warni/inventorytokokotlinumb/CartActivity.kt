// File: app/src/main/java/com/warni/inventorytokokotlinumb/CartActivity.kt
package com.warni.inventorytokokotlinumb // Ubah package ini

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.warni.inventorytokokotlinumb.adapters.CartAdapter // Sesuaikan import
import com.warni.inventorytokokotlinumb.network.RetrofitClient // Sesuaikan import
import com.warni.inventorytokokotlinumb.viewmodels.CartViewModel // Sesuaikan import
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var totalTextView: TextView
    private lateinit var checkoutButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyCartTextView: TextView

    private lateinit var cartAdapter: CartAdapter
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Setup ActionBar
        supportActionBar?.title = "Keranjang Belanja"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tombol kembali

        // Inisialisasi View
        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        totalTextView = findViewById(R.id.totalTextView)
        checkoutButton = findViewById(R.id.checkoutButton)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyCartTextView = findViewById(R.id.emptyCartTextView)

        // Setup RecyclerView
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(
            cartItems = mutableListOf(),
            onDeleteItemClick = { cartItem ->
                showDeleteCartItemConfirmation(cartItem.cartId, cartItem.productName)
            }
        )
        cartRecyclerView.adapter = cartAdapter

        // Observasi LiveData dari ViewModel
        cartViewModel.cartItems.observe(this) { items ->
            items?.let {
                cartAdapter.updateCartItems(it)
                updateTotal(it.sumOf { item -> item.subtotal.toDouble() })
                emptyCartTextView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                checkoutButton.isEnabled = it.isNotEmpty()
            }
        }

        cartViewModel.isLoading.observe(this) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            checkoutButton.isEnabled = !isLoading && (cartViewModel.cartItems.value?.isNotEmpty() ?: false)
        }

        cartViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                cartViewModel.clearErrorMessage()
                loadCartItems() // Muat ulang keranjang setelah ada pesan (termasuk sukses delete)
            }
        }

        cartViewModel.purchaseSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Pembelian berhasil!", Toast.LENGTH_LONG).show()
                loadCartItems()
                cartViewModel.clearPurchaseSuccess()
            }
        }

        checkoutButton.setOnClickListener {
            showPurchaseConfirmation()
        }

        // Muat item keranjang saat Activity dibuat
        loadCartItems()
    }

    // Handle tombol kembali di ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadCartItems() {
        val userId = getUserId()
        if (userId != -1) {
            cartViewModel.loadCartItems(userId)
        } else {
            Toast.makeText(this, "User ID tidak ditemukan. Harap login kembali.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getUserId(): Int {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    private fun updateTotal(total: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        totalTextView.text = "Total: ${format.format(total)}"
    }

    private fun showDeleteCartItemConfirmation(cartItemId: Int, productName: String) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Item")
            .setMessage("Apakah Anda yakin ingin menghapus '$productName' dari keranjang?")
            .setPositiveButton("Hapus") { dialog, _ ->
                cartViewModel.deleteCartItem(cartItemId)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showPurchaseConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembelian")
            .setMessage("Apakah Anda yakin ingin melanjutkan pembelian semua item di keranjang?")
            .setPositiveButton("Beli Sekarang") { dialog, _ ->
                cartViewModel.makePurchase()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
