package com.example.geekshop.data.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.geekshop.R
import com.example.geekshop.data.model.Products

class ProductAdapter(
    private var products: List<Products> = emptyList(),
    private val listener: ProductActionListener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    interface ProductActionListener {
        fun onProductClick(product: Products)
        fun onAddToCartClick(product: Products)
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.nameProd)
        private val priceView: TextView = itemView.findViewById(R.id.costProd)
        private val imageView: ImageView = itemView.findViewById(R.id.imageProd)
        private val addToCartBtn: Button = itemView.findViewById(R.id.AddToCart)

        fun bind(product: Products) {
            nameView.text = product.Name
            priceView.text = product.Cost
            imageView.setImageResource(product.imageId)

            itemView.setOnClickListener { listener.onProductClick(product) }
            addToCartBtn.setOnClickListener { listener.onAddToCartClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.style_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Products>) {
        val diffCallback = ProductDiffCallback(products, newProducts)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        products = newProducts
        diffResult.dispatchUpdatesTo(this)
    }

    private class ProductDiffCallback(
        private val oldList: List<Products>,
        private val newList: List<Products>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}