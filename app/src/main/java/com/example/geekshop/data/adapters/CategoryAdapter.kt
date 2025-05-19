package com.example.geekshop.data.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.geekshop.data.model.Category
import com.example.geekshop.R

/**
 * Адаптер для отображения списка категорий в горизонтальном RecyclerView
 */
class CategoryAdapter(
    private var categories: List<Category> = emptyList(),
    private val listener: CategoryActionListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    /**
     * Интерфейс для обработки кликов по категориям
     */
    interface CategoryActionListener {
        fun onCategoryClick(category: Category)
    }

    /**
     * ViewHolder для отдельной категории
     */
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.titleCategory)

        /**
         * Привязка данных категории к View
         */
        fun bind(category: Category) {
            nameView.text = category.Title
            itemView.setOnClickListener { listener.onCategoryClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.style_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    /**
     * Обновление списка категорий с анимацией изменений
     */
    fun updateCategories(newCategories: List<Category>) {
        val diffCallback = CategoryDiffCallback(categories, newCategories)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        categories = newCategories
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Callback для сравнения старых и новых данных категорий
     */
    private class CategoryDiffCallback(
        private val oldList: List<Category>,
        private val newList: List<Category>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].Title == newList[newItemPosition].Title
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}