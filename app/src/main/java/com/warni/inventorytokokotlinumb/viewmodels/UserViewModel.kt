// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/viewmodels/UserViewModel.kt
package com.warni.inventorytokokotlinumb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warni.inventorytokokotlinumb.models.User
import com.warni.inventorytokokotlinumb.network.RetrofitClient
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun loadUsers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getUsers()
                if (response.isSuccessful) {
                    _users.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal memuat pengguna: ${response.code()} - ${errorBody}")
                    Log.e("UserViewModel", "Error loading users: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("UserViewModel", "Network error loading users", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteUser(userId: Int, username: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteUser(userId)
                if (response.isSuccessful) {
                    _errorMessage.postValue("Pengguna '$username' berhasil dihapus!")
                    loadUsers() // Muat ulang daftar pengguna setelah penghapusan
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal menghapus pengguna: ${response.code()} - ${errorBody}")
                    Log.e("UserViewModel", "Error deleting user: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("UserViewModel", "Network error deleting user", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
