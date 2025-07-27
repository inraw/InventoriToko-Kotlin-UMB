// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/adapters/ProductAdapter.kt
package com.ahmar.inventorytokokotlinumb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton // Import ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ahmar.inventorytokokotlinumb.R
import com.ahmar.inventorytokokotlinumb.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: MutableList<Product>,
    private val userRole: String,
    private val onAddToCartClick: (Product) -> Unit,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit // Lambda untuk delete
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.productPriceTextView)
        val productStockTextView: TextView = itemView.findViewById(R.id.productStockTextView)
        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
        val editProductButton: ImageButton = itemView.findViewById(R.id.editProductButton) // Ubah ke ImageButton
        val deleteProductButton: ImageButton = itemView.findViewById(R.id.deleteProductButton) // Ubah ke ImageButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productNameTextView.text = product.name

        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")) // Format Rupiah Indonesia
        holder.productPriceTextView.text = "Harga: ${format.format(product.price)}"
        holder.productStockTextView.text = "Stok: ${product.stock}"

        // Muat gambar produk dari URL menggunakan Glide
        product.imageUrl?.let { url ->
            Glide.with(holder.itemView.context)
                .load(url)
                .placeholder(R.drawable.ic_launcher_background) // Gambar placeholder saat loading
                .error(R.drawable.ic_launcher_background) // Gambar jika terjadi error
                .into(holder.productImageView)
        } ?: holder.productImageView.setImageResource(R.drawable.ic_launcher_background) // Jika URL null

        // Atur visibilitas tombol berdasarkan peran pengguna
        if (userRole == "customer") {
            holder.addToCartButton.visibility = View.VISIBLE
            holder.editProductButton.visibility = View.GONE
            holder.deleteProductButton.visibility = View.GONE // Sembunyikan untuk customer
            holder.addToCartButton.setOnClickListener {
                onAddToCartClick(product)
            }
        } else if (userRole == "admin") {
            holder.addToCartButton.visibility = View.GONE
            holder.editProductButton.visibility = View.VISIBLE
            holder.deleteProductButton.visibility = View.VISIBLE // Tampilkan untuk admin
            holder.editProductButton.setOnClickListener {
                onEditClick(product)
            }
            holder.deleteProductButton.setOnClickListener { // Atur listener untuk tombol hapus
                onDeleteClick(product)
            }
        } else {
            // Sembunyikan semua tombol jika peran tidak dikenal atau tidak ada
            holder.addToCartButton.visibility = View.GONE
            holder.editProductButton.visibility = View.GONE
            holder.deleteProductButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}
