// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/EditProductDialogFragment.kt
package com.ahmar.inventorytokokotlinumb

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
import com.ahmar.inventorytokokotlinumb.models.Product
import com.ahmar.inventorytokokotlinumb.viewmodels.ProductViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditProductDialogFragment : DialogFragment() {

    private lateinit var productNameEditText: TextInputEditText
    private lateinit var productDescriptionEditText: TextInputEditText
    private lateinit var productPriceEditText: TextInputEditText
    private lateinit var productStockEditText: TextInputEditText
    private lateinit var productImageUrlEditText: TextInputEditText
    private lateinit var saveProductButton: Button
    private lateinit var cancelButton: Button
    private lateinit var loadingProgressBar: ProgressBar

    private val productViewModel: ProductViewModel by activityViewModels()

    private var productToEdit: Product? = null

    companion object {
        private const val ARG_PRODUCT = "product_to_edit"

        fun newInstance(product: Product): EditProductDialogFragment {
            val fragment = EditProductDialogFragment()
            val args = Bundle().apply {
                putParcelable(ARG_PRODUCT, product)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productToEdit = it.getParcelable(ARG_PRODUCT)
        }
    }

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

        productToEdit?.let { product ->
            productNameEditText.setText(product.name)
            productDescriptionEditText.setText(product.description)
            productPriceEditText.setText(product.price.toString())
            productStockEditText.setText(product.stock.toString())
            productImageUrlEditText.setText(product.imageUrl)
            builder.setTitle("Edit Produk: ${product.name}")
            saveProductButton.text = "Simpan Perubahan"
        } ?: run {
            builder.setTitle("Edit Produk")
            saveProductButton.text = "Simpan"
        }

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
            saveChanges()
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

    private fun saveChanges() {
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

        productToEdit?.let { existingProduct ->
            val updatedProduct = Product(
                id = existingProduct.id,
                name = name,
                description = if (description.isEmpty()) null else description,
                price = price,
                stock = stock,
                imageUrl = if (imageUrl.isEmpty()) null else imageUrl,
                createdAt = existingProduct.createdAt // <--- TAMBAHKAN INI
            )
            productViewModel.updateProduct(updatedProduct)
        } ?: run {
            Toast.makeText(context, "Produk tidak ditemukan untuk diedit.", Toast.LENGTH_SHORT).show()
        }
    }
}
