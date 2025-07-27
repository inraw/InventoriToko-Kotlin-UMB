// File: app/src/main/java/com/example/inventorytokokotlinumb/CartActivity.kt
package com.ahmar.inventorytokokotlinumb

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
import com.ahmar.inventorytokokotlinumb.adapters.CartAdapter
import com.ahmar.inventorytokokotlinumb.network.RetrofitClient
import com.ahmar.inventorytokokotlinumb.viewmodels.CartViewModel
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
                cartViewModel.clearErrorMessage() // Bersihkan pesan setelah ditampilkan
                // Jika pesan error bukan dari pembelian/penghapusan yang gagal,
                // atau jika itu adalah pesan sukses penghapusan, muat ulang keranjang.
                // Kita bisa lebih spesifik di sini jika perlu, tapi untuk saat ini,
                // setiap kali ada pesan, kita coba muat ulang.
                loadCartItems() // PENTING: Muat ulang keranjang setelah ada pesan (termasuk sukses delete)
            }
        }

        cartViewModel.purchaseSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Pembelian berhasil!", Toast.LENGTH_LONG).show()
                // Setelah pembelian berhasil, muat ulang keranjang (seharusnya kosong)
                loadCartItems() // PENTING: Muat ulang keranjang setelah pembelian berhasil
                cartViewModel.clearPurchaseSuccess() // Bersihkan status
            }
        }

        checkoutButton.setOnClickListener {
            showPurchaseConfirmation()
        }

        // Muat item keranjang saat Activity dibuat
        loadCartItems() // Panggil fungsi pembantu untuk memuat keranjang
    }

    // Handle tombol kembali di ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Fungsi pembantu untuk mendapatkan userId dan memuat item keranjang
    private fun loadCartItems() {
        val userId = getUserId()
        if (userId != -1) {
            cartViewModel.loadCartItems(userId)
        } else {
            Toast.makeText(this, "User ID tidak ditemukan. Harap login kembali.", Toast.LENGTH_LONG).show()
            // Opsional: Arahkan kembali ke LoginActivity jika userId tidak valid
            // val intent = Intent(this, LoginActivity::class.java)
            // startActivity(intent)
            // finish()
        }
    }

    private fun getUserId(): Int {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", -1)
    }

    private fun updateTotal(total: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")) // Format Rupiah Indonesia
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
