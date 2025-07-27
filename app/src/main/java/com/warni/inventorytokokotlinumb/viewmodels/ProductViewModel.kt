// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/viewmodels/ProductViewModel.kt
package com.warni.inventorytokokotlinumb.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warni.inventorytokokotlinumb.models.Product
import com.warni.inventorytokokotlinumb.network.RetrofitClient
import kotlinx.coroutines.launch
import com.warni.inventorytokokotlinumb.models.AddToCartRequest

class ProductViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _productOperationSuccess = MutableLiveData<Boolean>()
    val productOperationSuccess: LiveData<Boolean> = _productOperationSuccess

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearProductOperationSuccess() {
        _productOperationSuccess.value = false
    }

    fun loadProducts(role: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = if (role == "admin") {
                    RetrofitClient.instance.getAdminProducts()
                } else {
                    RetrofitClient.instance.getCustomerProducts()
                }

                if (response.isSuccessful) {
                    _products.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal memuat produk: ${response.code()} - ${errorBody}")
                    Log.e("ProductViewModel", "Error loading products: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("ProductViewModel", "Network error loading products", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun addProduct(product: Product) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.addProduct(product)
                if (response.isSuccessful) {
                    _errorMessage.postValue("Produk '${product.name}' berhasil ditambahkan!")
                    _productOperationSuccess.postValue(true)
                    loadProducts("admin")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal menambahkan produk: ${response.code()} - ${errorBody}")
                    Log.e("ProductViewModel", "Error adding product: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("ProductViewModel", "Network error adding product", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updateProduct(product: Product) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateProduct(product.id, product)
                if (response.isSuccessful) {
                    _errorMessage.postValue("Produk '${product.name}' berhasil diperbarui!")
                    _productOperationSuccess.postValue(true)
                    loadProducts("admin")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal memperbarui produk: ${response.code()} - ${errorBody}")
                    Log.e("ProductViewModel", "Error updating product: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("ProductViewModel", "Network error updating product", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Fungsi baru untuk menghapus produk
    fun deleteProduct(productId: Int, productName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteProduct(productId)
                if (response.isSuccessful) {
                    _errorMessage.postValue("Produk '$productName' berhasil dihapus!")
                    loadProducts("admin") // Muat ulang produk setelah penghapusan
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal menghapus produk: ${response.code()} - ${errorBody}")
                    Log.e("ProductViewModel", "Error deleting product: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("ProductViewModel", "Network error deleting product", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun addProductToCart(productId: Int, quantity: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = AddToCartRequest(productId, quantity)
                val response = RetrofitClient.instance.addProductToCart(request)
                if (response.isSuccessful) {
                    _errorMessage.postValue("Produk berhasil ditambahkan ke keranjang!")
                    loadProducts("customer")
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.postValue("Gagal menambahkan ke keranjang: ${response.code()} - ${errorBody}")
                    Log.e("ProductViewModel", "Error adding to cart: ${response.code()} - ${errorBody}")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Terjadi kesalahan jaringan: ${e.message}")
                Log.e("ProductViewModel", "Network error adding to cart", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
