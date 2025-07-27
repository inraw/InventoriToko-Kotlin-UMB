// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/adapters/UserAdapter.kt
package com.warni.inventorytokokotlinumb.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.warni.inventorytokokotlinumb.R
import com.warni.inventorytokokotlinumb.models.User

class UserAdapter(
    private val users: MutableList<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        val userRoleTextView: TextView = itemView.findViewById(R.id.userRoleTextView)
        val deleteUserButton: ImageButton = itemView.findViewById(R.id.deleteUserButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.usernameTextView.text = user.username
        holder.userRoleTextView.text = "Peran: ${user.role}"

        // Admin tidak bisa menghapus dirinya sendiri atau admin lain melalui endpoint ini
        // Asumsi endpoint delete user hanya untuk customer
        if (user.role == "customer") {
            holder.deleteUserButton.visibility = View.VISIBLE
            holder.deleteUserButton.setOnClickListener {
                onDeleteClick(user)
            }
        } else {
            holder.deleteUserButton.visibility = View.GONE // Sembunyikan tombol hapus untuk admin
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}
