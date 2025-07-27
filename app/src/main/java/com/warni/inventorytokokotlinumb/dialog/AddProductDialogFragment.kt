// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/AddProductDialogFragment.kt
package com.warni.inventorytokokotlinumb

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.warni.inventorytokokotlinumb.models.Product
import com.warni.inventorytokokotlinumb.viewmodels.ProductViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddProductDialogFragment : DialogFragment() {

    private lateinit var productNameEditText: TextInputEditText
    private lateinit var productDescriptionEditText: TextInputEditText
    private lateinit var productPriceEditText: TextInputEditText
    private lateinit var productStockEditText: TextInputEditText
    private lateinit var productImageUrlEditText: TextInputEditText
    private lateinit var saveProductButton: Button
    private lateinit var cancelButton: Button
    private lateinit var loadingProgressBar: ProgressBar

    private val productViewModel: ProductViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_product_form, null)

        productNameEditText = view.findViewById(R.id.productNameEditText)
        productDescriptionEditText = view.findViewById(R.id.productDescriptionEditText)
        productPriceEditText = view.findViewById(R.id.productPriceEditText)
        productStockEditText = view.findViewById(R.id.productStockEditText)
        productImageUrlEditText = view.findViewById(R.id.productImageUrlEditText)
        saveProductButton = view.findViewById(R.id.saveProductButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)

        saveProductButton.text = "Tambah Produk"

        productViewModel.isLoading.observe(this) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveProductButton.isEnabled = !isLoading
            cancelButton.isEnabled = !isLoading
        }

        productViewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                productViewModel.clearErrorMessage()
            }
        }

        productViewModel.productOperationSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                dismiss()
                productViewModel.clearProductOperationSuccess()
            }
        }

        saveProductButton.setOnClickListener {
            saveProduct()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun saveProduct() {
        val name = productNameEditText.text.toString().trim()
        val description = productDescriptionEditText.text.toString().trim()
        val priceStr = productPriceEditText.text.toString().trim()
        val stockStr = productStockEditText.text.toString().trim()
        val imageUrl = productImageUrlEditText.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(context, "Nama, Harga, dan Stok tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (price == null || price <= 0) {
            Toast.makeText(context, "Harga tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        if (stock == null || stock < 0) {
            Toast.makeText(context, "Stok tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val newProduct = Product(
            id = 0, // ID akan di-generate oleh backend
            name = name,
            description = if (description.isEmpty()) null else description,
            price = price,
            stock = stock,
            imageUrl = if (imageUrl.isEmpty()) null else imageUrl,
            createdAt = null // <--- TAMBAHKAN INI
        )

        productViewModel.addProduct(newProduct)
    }
}
