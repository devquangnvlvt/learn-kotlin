package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.R
import com.example.kotlinv4.databinding.ItemColorBackgroundBinding
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
            .load(item)                          // "file:///android_asset/background/xxx.png"
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .centerCrop()
            .into(binding.imageSticker)

        binding.root.setOnClickListener { onClickSticker(item) }
    }
}