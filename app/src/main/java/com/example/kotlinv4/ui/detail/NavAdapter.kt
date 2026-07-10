package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.databinding.ItemOptionBinding
import com.example.kotlinv4.ui.base.BaseAdapter
import com.example.kotlinv4.ui.model.CategoryModel
import com.example.kotlinv4.ui.utils.KeyApp

/**
 * rvTabs (thanh nav): danh sách các folder x-y trong 1 nhóm, ảnh = nav.webp, sắp theo y (navOrder).
 */
class NavAdapter(
    private val onClickItem: (CategoryModel) -> Unit
) : BaseAdapter<CategoryModel, ItemOptionBinding>(ItemOptionBinding::inflate) {

    private var selectedParts: String? = null

    fun setSelected(parts: String) {
        selectedParts = parts
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemOptionBinding, item: CategoryModel, position: Int) {
        Glide.with(binding.imageOption.context)
            .load(KeyApp.getNavUrl(item.position, item.parts))
            .into(binding.imageOption)

        val isSelected = item.parts == selectedParts
        binding.cardOption.strokeWidth = if (isSelected) 4 else 0

        //  binding.root.setOnClickListener = gắn click lên toàn bộ item
        binding.root.setOnClickListener { onClickItem(item) }
    }
}