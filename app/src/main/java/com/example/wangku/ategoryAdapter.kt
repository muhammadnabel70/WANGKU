package com.example.wangku // Sesuaikan dengan package Anda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wangku.databinding.ListItemCategoryBinding

// 1. TAMBAHKAN 'onItemClick' di constructor
class CategoryAdapter(
    private var categories: List<Category>,
    private val onItemClick: (Category) -> Unit // Ini adalah lambda function
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ListItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ListItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.binding.apply {
            tvCategoryName.text = category.name
            ivCategoryIcon.setImageResource(category.iconRes)
        }

        // 2. TAMBAHKAN OnClickListener
        holder.itemView.setOnClickListener {
            onItemClick(category) // Panggil lambda dengan data kategori yang diklik
        }
    }

    // Fungsi updateData tetap sama
    fun updateData(newCategories: List<Category>) {
        this.categories = newCategories
        notifyDataSetChanged()
    }
}