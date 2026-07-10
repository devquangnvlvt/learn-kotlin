package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.databinding.ItemOptionBinding
import com.example.kotlinv4.ui.base.BaseAdapter
import com.example.kotlinv4.ui.utils.KeyApp

/**
 * rvOptions: danh sách ảnh 1..quantity của folder x-y đang chọn ở rvTabs.
 * Thumbnail dùng chính ảnh thật (không có nav riêng), tự chọn thủ công.
 */
class QuantityAdapter(
    private val groupKey: String,
    val parts: String,      // expose để Activity so sánh, tránh rebuild adapter không cần thiết
    val color: String?,     // expose để Activity so sánh
    private val onClickIndex: (Int) -> Unit
) : BaseAdapter<Int, ItemOptionBinding>(ItemOptionBinding::inflate) {

    private var selectedIndex: Int? = null

    fun setSelected(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemOptionBinding, item: Int, position: Int) {
        Glide.with(binding.imageOption.context)
            .load(KeyApp.getLayerUrl(groupKey, parts, item, color))
            .into(binding.imageOption)

        val isSelected = item == selectedIndex
        binding.cardOption.strokeWidth = if (isSelected) 4 else 0

        binding.root.setOnClickListener { onClickIndex(item) }
    }
}