package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.databinding.ItemNavBinding
import com.example.kotlinv4.ui.base.BaseAdapter
import com.example.kotlinv4.ui.model.CategoryModel
import com.example.kotlinv4.ui.utils.KeyApp

/**
 * rvNavs: danh sách các folder x-y trong 1 nhóm, ảnh = nav.webp, sắp theo navOrder.
 */
class NavAdapter(
    private val onClickItem: (CategoryModel) -> Unit
) : BaseAdapter<CategoryModel, ItemNavBinding>(ItemNavBinding::inflate) {

    private var selectedNav: String? = null

    fun setSelected(parts: String) {
        selectedNav = parts
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemNavBinding, item: CategoryModel, position: Int) {
        Glide.with(binding.imageNav.context)
            .load(KeyApp.getNavUrl(item.position, item.parts))
            .into(binding.imageNav)

        val isSelected = item.parts == selectedNav
        binding.cardNav.strokeWidth = if (isSelected) 4 else 0

        binding.root.setOnClickListener { onClickItem(item) }
    }
}
