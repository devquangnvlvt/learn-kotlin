package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.R
import com.example.kotlinv4.databinding.ItemStickerBinding
import com.example.kotlinv4.ui.base.BaseAdapter

class StickerAdapter(
    private val onClickSticker: (String) -> Unit
) : BaseAdapter<String, ItemStickerBinding>(ItemStickerBinding::inflate) {

    private var selectedSticker: String? = null

    fun setSelected(hex: String) {
        selectedSticker = hex
        notifyDataSetChanged()
    }
    override fun onBind(binding: ItemStickerBinding, item: String, position: Int) {
        Glide.with(binding.imageSticker.context)
            .load(item)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .centerCrop()
            .into(binding.imageSticker)

        val isSelected = item == selectedSticker
        binding.boxSticker.background = if (isSelected)
            androidx.core.content.ContextCompat.getDrawable(binding.boxSticker.context, R.drawable.bg_item_selected)
        else null

        binding.root.setOnClickListener { onClickSticker(item) }
    }
}