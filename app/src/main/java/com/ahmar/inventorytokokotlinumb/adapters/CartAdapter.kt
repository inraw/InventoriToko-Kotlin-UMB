// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/adapters/CartAdapter.kt
package com.ahmar.inventorytokokotlinumb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmar.inventorytokokotlinumb.R
import com.ahmar.inventorytokokotlinumb.models.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onDeleteItemClick: ((CartItem) -> Unit)? = null
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.cartProductNameTextView)
        val productPriceTextView: TextView = itemView.findViewById(R.id.cartProductPriceTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.cartQuantityTextView)
        val subtotalTextView: TextView = itemView.findViewById(R.id.cartSubtotalTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteCartItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.productNameTextView.text = item.productName
        holder.quantityTextView.text = "Jumlah: ${item.quantity}"

        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")) // Format Rupiah Indonesia
        holder.productPriceTextView.text = format.format(item.price.toDouble())
        holder.subtotalTextView.text = format.format(item.subtotal.toDouble())

        holder.deleteButton.setOnClickListener {
            onDeleteItemClick?.invoke(item)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newItems: List<CartItem>) {
        cartItems.clear()
        cartItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
