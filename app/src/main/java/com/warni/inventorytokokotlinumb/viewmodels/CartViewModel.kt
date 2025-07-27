// File: app/src/main/java/com/example/inventorytokokotlinumb/viewmodels/CartViewModel.kt
package com.warni.inventorytokokotlinumb.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warni.inventorytokokotlinumb.models.CartItem
import com.warni.inventorytokokotlinumb.network.RetrofitClient
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> = _purchaseSuccess

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearPurchaseSuccess() {
        _purchaseSuccess.value = false
    }

    fun loadCartItems(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Pastikan userId valid sebelum memanggil API
                if (userId == -1) {
                    _errorMessage.postValue("User ID tidak ditemukan. Harap login kembali.")
                    return@launch
                }
                val response = RetrofitClient.instance.getCustomerCart()
                if (response.isSuccessful) {
                    _cartItems.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal memuat keranjang: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteCartItem(cartItemId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteCartItem(cartItemId)
                if (response.isSuccessful) {
                    _errorMessage.postValue("Item berhasil dihapus dari keranjang!") // Sinyal keberhasilan
                    // TIDAK PERLU memanggil loadCartItems di sini
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal menghapus item: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan saat menghapus item: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun makePurchase() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.makePurchase()
                if (response.isSuccessful) {
                    _purchaseSuccess.postValue(true) // Sinyal keberhasilan pembelian
                    // TIDAK PERLU memanggil loadCartItems di sini
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal melakukan pembelian: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan saat pembelian: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
