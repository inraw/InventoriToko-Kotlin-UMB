// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/UserManagementActivity.kt
package com.warni.inventorytokokotlinumb

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.warni.inventorytokokotlinumb.adapters.UserAdapter
import com.warni.inventorytokokotlinumb.models.User
import com.warni.inventorytokokotlinumb.viewmodels.UserViewModel

class UserManagementActivity : AppCompatActivity() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var emptyUsersTextView: TextView

    private lateinit var userAdapter: UserAdapter
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        // Setup ActionBar
        supportActionBar?.title = "Manajemen Pengguna"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tombol kembali

        // Inisialisasi View
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        emptyUsersTextView = findViewById(R.id.emptyUsersTextView)

        // Setup RecyclerView
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(
            users = mutableListOf(),
            onDeleteClick = { user ->
                showDeleteUserConfirmation(user)
            }
        )
        usersRecyclerView.adapter = userAdapter

        // Observasi LiveData dari ViewModel
        userViewModel.users.observe(this) { users ->
            users?.let {
                userAdapter.updateUsers(it)
                emptyUsersTextView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        userViewModel.isLoading.observe(this) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        userViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                userViewModel.clearErrorMessage()
            }
        }

        // Muat pengguna saat Activity dibuat
        userViewModel.loadUsers()
    }

    // Handle tombol kembali di ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun showDeleteUserConfirmation(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pengguna")
            .setMessage("Apakah Anda yakin ingin menghapus pengguna '${user.username}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                userViewModel.deleteUser(user.id, user.username)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}