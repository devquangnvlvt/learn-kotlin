package com.example.kotlinv4.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinv4.databinding.ItemLayerBinding
import com.example.kotlinv4.databinding.ItemLayerClearBinding
import com.example.kotlinv4.ui.utils.KeyApp

/**
 * rvOptions: item đầu tiên là nút hủy layer, các item còn lại là thumbnail ảnh.
 * - TYPE_CLEAR  (position 0) : nút X hủy layer
 * - TYPE_LAYER  (position 1+): thumbnail ảnh bình thường
 */
class LayerAdapter(
    private val groupKey: String,
    val parts: String,
    val color: String?,
    val clearable: Boolean = true,
    private val onClear: () -> Unit,
    private val onClickIndex: (Int) -> Unit
) : ListAdapter<Int, RecyclerView.ViewHolder>(DIFF) {

    private var selectedIndex: Int? = null
    private var isCleared: Boolean = false

    fun setSelected(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }

    fun setCleared(cleared: Boolean) {
        if (isCleared != cleared) {
            isCleared = cleared
            notifyDataSetChanged() // refresh toàn bộ để sync border
        }
    }

    // ── View types ────────────────────────────────────────────────────────────

    override fun getItemViewType(position: Int): Int =
        if (position == 0 && clearable) TYPE_CLEAR else TYPE_LAYER

    override fun getItemCount(): Int = super.getItemCount() + if (clearable) 1 else 0

    // position trong list thật = position - 1 (bỏ qua item clear ở đầu)
    override fun getItem(position: Int): Int = super.getItem(if (clearable) position - 1 else position)

    // ── ViewHolder creation ───────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_CLEAR) {
            ClearViewHolder(ItemLayerClearBinding.inflate(inflater, parent, false))
        } else {
            LayerViewHolder(ItemLayerBinding.inflate(inflater, parent, false))
        }
    }

    // ── Binding ───────────────────────────────────────────────────────────────

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ClearViewHolder -> holder.bind(isCleared)
            is LayerViewHolder -> holder.bind(getItem(position))
        }
    }

    inner class ClearViewHolder(
        private val binding: ItemLayerClearBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cleared: Boolean) {
            // Có border khi cleared, đồng thời thumbnail sẽ không có border
            binding.cardClear.strokeWidth = if (cleared) 4 else 0
            binding.root.setOnClickListener { onClear() }
        }
    }

    inner class LayerViewHolder(
        private val binding: ItemLayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(index: Int) {
            Glide.with(binding.imageLayer.context)
                .load(KeyApp.getLayerUrl(groupKey, parts, index, color))
                .into(binding.imageLayer)

            // Chỉ có border khi được chọn VÀ layer không đang bị cleared
            binding.cardLayer.strokeWidth = if (!isCleared && index == selectedIndex) 4 else 0
            binding.root.setOnClickListener { onClickIndex(index) }
        }
    }

    companion object {
        private const val TYPE_CLEAR = 0
        private const val TYPE_LAYER = 1

        private val DIFF = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        }
    }
}
