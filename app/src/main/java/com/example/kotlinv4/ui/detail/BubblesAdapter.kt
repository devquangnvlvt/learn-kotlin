package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.R
import com.example.kotlinv4.databinding.ItemBubblesBinding
import com.example.kotlinv4.ui.base.BaseAdapter

class BubblesAdapter(
    private val onClickBubbles: (String) -> Unit
): BaseAdapter<String, ItemBubblesBinding>(ItemBubblesBinding::inflate) {

    private var selectedBubbles: String? = null

    fun setSelected(color: String) {
        selectedBubbles = color
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemBubblesBinding, item: String, position: Int) {
        Glide.with(binding.imageBubbles.context)
            .load(item)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .centerCrop()
            .into(binding.imageBubbles)

            var isSelected = item == selectedBubbles

            binding.boxBubbles.background = if (isSelected)
                androidx.core.content.ContextCompat.getDrawable(binding.boxBubbles.context, R.drawable.bg_item_selected)
            else null

        binding.root.setOnClickListener {
            onClickBubbles(item)
        }

    }
}