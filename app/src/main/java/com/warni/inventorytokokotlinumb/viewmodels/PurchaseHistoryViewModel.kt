// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/viewmodels/PurchaseHistoryViewModel.kt
package com.warni.inventorytokokotlinumb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warni.inventorytokokotlinumb.models.Purchase
import com.warni.inventorytokokotlinumb.network.RetrofitClient
import kotlinx.coroutines.launch

class PurchaseHistoryViewModel : ViewModel() {

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>> = _purchases

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Fungsi untuk memuat riwayat pembelian customer
    fun loadCustomerPurchases() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getCustomerPurchases()
                if (response.isSuccessful) {
                    _purchases.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal memuat riwayat customer: ${response.code()} - ${errorBody}")
                    Log.e("PurchaseHistoryViewModel", "Error loading customer purchases: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("PurchaseHistoryViewModel", "Network error loading customer purchases", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Fungsi baru untuk memuat riwayat pembelian admin (semua pembelian)
    fun loadAdminPurchases() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAdminPurchases()
                if (response.isSuccessful) {
                    _purchases.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal memuat riwayat admin: ${response.code()} - ${errorBody}")
                    Log.e("PurchaseHistoryViewModel", "Error loading admin purchases: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("PurchaseHistoryViewModel", "Network error loading admin purchases", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
