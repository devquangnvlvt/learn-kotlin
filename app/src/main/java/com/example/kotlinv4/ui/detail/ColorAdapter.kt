package com.example.kotlinv4.ui.detail

import android.graphics.Color
import com.example.kotlinv4.databinding.ItemColorBinding
import com.example.kotlinv4.ui.base.BaseAdapter

class ColorAdapter(
    private val onClickColor: (String) -> Unit
) : BaseAdapter<String, ItemColorBinding>(ItemColorBinding::inflate) {

    private var selectedColor: String? = null

    fun setSelected(color: String) {
        selectedColor = color
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemColorBinding, item: String, position: Int) {
        try {
            binding.viewColorDot.setBackgroundColor(Color.parseColor("#$item"))
        } catch (e: Exception) {
            binding.viewColorDot.setBackgroundColor(Color.LTGRAY)
        }

        val isSelected = item == selectedColor
        binding.cardColor.strokeWidth = if (isSelected) 4 else 0

        binding.root.setOnClickListener { onClickColor(item) }
    }
}