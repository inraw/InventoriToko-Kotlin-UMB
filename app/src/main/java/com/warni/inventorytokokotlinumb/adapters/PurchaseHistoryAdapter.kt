// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/adapters/PurchaseHistoryAdapter.kt
package com.warni.inventorytokokotlinumb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.warni.inventorytokokotlinumb.R
import com.warni.inventorytokokotlinumb.models.Purchase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PurchaseHistoryAdapter(
    private val purchases: MutableList<Purchase>
) : RecyclerView.Adapter<PurchaseHistoryAdapter.PurchaseViewHolder>() {

    companion object {
        private const val TAG = "PurchaseHistoryAdapter"
    }

    class PurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImageView: ImageView = itemView.findViewById(R.id.purchaseProductImageView)
        val productNameTextView: TextView = itemView.findViewById(R.id.purchaseProductNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.purchaseQuantityTextView)
        val totalPriceTextView: TextView = itemView.findViewById(R.id.purchaseTotalPriceTextView)
        val purchaseDateTextView: TextView = itemView.findViewById(R.id.purchaseDateTextView)
        val purchaseUsernameTextView: TextView = itemView.findViewById(R.id.purchaseUsernameTextView) // Inisialisasi TextView baru
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_purchase_history, parent, false)
        return PurchaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val purchase = purchases[position]
        holder.productNameTextView.text = purchase.productName
        holder.quantityTextView.text = "Jumlah: ${purchase.quantity}"

        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        holder.totalPriceTextView.text = purchase.totalPrice?.let {
            format.format(it)
        } ?: "Rp 0.00"

        // Atur visibilitas dan teks username
        purchase.username?.let { username ->
            holder.purchaseUsernameTextView.text = "Oleh: $username"
            holder.purchaseUsernameTextView.visibility = View.VISIBLE
        } ?: run {
            holder.purchaseUsernameTextView.visibility = View.GONE
        }

        try {
            val dateString = purchase.purchaseDate
            var parsedDate: java.util.Date? = null

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")

            try {
                parsedDate = isoFormat.parse(dateString)
            } catch (e: Exception) {
                val mysqlFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                try {
                    parsedDate = mysqlFormat.parse(dateString)
                } catch (e2: Exception) {
                    // Biarkan null
                }
            }

            val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            holder.purchaseDateTextView.text = "Tanggal: ${parsedDate?.let { outputFormat.format(it) } ?: "N/A"}"
        } catch (e: Exception) {
            holder.purchaseDateTextView.text = "Tanggal: ${purchase.purchaseDate}"
            e.printStackTrace()
        }

        purchase.imageUrl?.let { url ->
            Glide.with(holder.itemView.context)
                .load(url)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.productImageView)
        } ?: holder.productImageView.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun getItemCount(): Int = purchases.size

    fun updatePurchases(newPurchases: List<Purchase>) {
        purchases.clear()
        purchases.addAll(newPurchases)
        notifyDataSetChanged()
    }
}
