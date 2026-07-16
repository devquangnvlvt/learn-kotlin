package com.example.kotlinv4.ui.detail

import android.graphics.Color
import android.util.Log
import com.example.kotlinv4.databinding.ItemColorBackgroundBinding
import com.example.kotlinv4.ui.base.BaseAdapter

/**
 * Adapter cho rvColor (tab COULEUR) — hiển thị grid ô màu.
 * Item nhận hex string, tô màu nền ô đó.
 */
class ColorPickerColorAdapter(
    private val onClickColor: (String) -> Unit
) : BaseAdapter<String, ItemColorBackgroundBinding>(ItemColorBackgroundBinding::inflate) {

    private var selectedHex: String? = null

    fun setSelected(hex: String) {
        selectedHex = hex
        notifyDataSetChanged()
    }
    override fun onBind(binding: ItemColorBackgroundBinding, item: String, position: Int) {

        try {
            var color = item == selectedHex

            binding.colorItem.strokeWidth = if (color) 4 else 0

            binding.root.setBackgroundColor(Color.parseColor("#$item"))
        } catch (e: Exception) {
            binding.root.setBackgroundColor(Color.LTGRAY)
        }
        binding.root.setOnClickListener { onClickColor(item) }
    }
}
