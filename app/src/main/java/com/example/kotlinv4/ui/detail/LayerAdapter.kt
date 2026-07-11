package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.databinding.ItemLayerBinding
import com.example.kotlinv4.ui.base.BaseAdapter
import com.example.kotlinv4.ui.utils.KeyApp

/**
 * rvOptions: danh sách ảnh 1..quantity của folder x-y đang chọn ở rvNavs.
 * Thumbnail dùng chính ảnh thật (không có nav riêng), tự chọn thủ công.
 */
class LayerAdapter(
    private val groupKey: String,
    val parts: String,      // expose để Activity so sánh, tránh rebuild adapter không cần thiết
    val color: String?,     // expose để Activity so sánh
    private val onClickIndex: (Int) -> Unit
) : BaseAdapter<Int, ItemLayerBinding>(ItemLayerBinding::inflate) {

    private var selectedIndex: Int? = null

    fun setSelected(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemLayerBinding, item: Int, position: Int) {
        Glide.with(binding.imageLayer.context)
            .load(KeyApp.getLayerUrl(groupKey, parts, item, color))
            .into(binding.imageLayer)

        val isSelected = item == selectedIndex
        binding.cardLayer.strokeWidth = if (isSelected) 4 else 0

        binding.root.setOnClickListener { onClickIndex(item) }
    }
}
