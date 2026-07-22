package com.example.kotlinv4.ui.category

import com.bumptech.glide.Glide
import com.example.kotlinv4.ui.utils.KeyApp
import com.example.kotlinv4.databinding.ItemCategoryBinding
import com.example.kotlinv4.ui.model.CategoryWithGroup
import com.example.kotlinv4.R
import com.example.kotlinv4.ui.base.BaseAdapter

class CategoryAdapter(
private val onClickItem:(CategoryWithGroup)-> Unit
): BaseAdapter<CategoryWithGroup, ItemCategoryBinding>(
    ItemCategoryBinding::inflate
){
    override fun onBind(binding: ItemCategoryBinding, item: CategoryWithGroup, position: Int) {
        var model = item.models
        val avatarUrl = KeyApp.getAvatarUrl(item.groupKey)

        Glide.with( binding.imageCategory.context)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .circleCrop()
            .into(binding.imageCategory)

        binding.imageCategory.setOnClickListener {
            onClickItem(item)
        }
    }
}