package com.example.kotlinv4.ui.core

import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinv4.databinding.ItemLayerClearBinding
import com.example.kotlinv4.databinding.ItemLayerRandomBinding

/**
 * ViewHolder dùng chung cho nút hủy layer.
 * Maker nào dùng LayerAdapter đều có thể tái sử dụng.
 */
class ClearViewHolder(
    private val binding: ItemLayerClearBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(isCleared: Boolean, onClick: () -> Unit) {
        binding.cardClear.strokeWidth = if (isCleared) 4 else 0
        binding.root.setOnClickListener { onClick() }
    }
}

/**
 * ViewHolder dùng chung cho nút random layer.
 * Maker nào dùng LayerAdapter đều có thể tái sử dụng.
 */
class RandomViewHolder(
    private val binding: ItemLayerRandomBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(onClick: () -> Unit) {
        binding.root.setOnClickListener { onClick() }
    }
}
