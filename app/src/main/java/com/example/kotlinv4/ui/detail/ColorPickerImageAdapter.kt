package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.R
import com.example.kotlinv4.databinding.ItemImageBackgroundBinding
import com.example.kotlinv4.ui.base.BaseAdapter

/**
 * Adapter cho rvImage (tab IMAGE) — hiển thị grid thumbnail ảnh background.
 * Item nhận path dạng "file:///android_asset/background/xxx.png"
 * Glide tự load được từ asset path này.
 */
class ColorPickerImageAdapter(
    private val onClickImage: (String) -> Unit
) : BaseAdapter<String, ItemImageBackgroundBinding>(ItemImageBackgroundBinding::inflate) {

    private var selectedImage: String? = null

    fun setSelected(hex: String) {
        selectedImage = hex
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemImageBackgroundBinding, item: String, position: Int) {
        Glide.with(binding.imageItem.context)
            .load(item)                          // "file:///android_asset/background/xxx.png"
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .centerCrop()
            .into(binding.imageItem)

        var color = item == selectedImage

//        binding.boxImage.strokeWidth = if (color) 4 else 0

        binding.root.setOnClickListener { onClickImage(item) }
    }
}
