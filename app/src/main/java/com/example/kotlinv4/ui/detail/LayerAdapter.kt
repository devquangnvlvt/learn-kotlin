package com.example.kotlinv4.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlinv4.databinding.ItemLayerBinding
import com.example.kotlinv4.databinding.ItemLayerClearBinding
import com.example.kotlinv4.databinding.ItemLayerRandomBinding
import com.example.kotlinv4.ui.core.ClearViewHolder
import com.example.kotlinv4.ui.core.RandomViewHolder
import com.example.kotlinv4.ui.utils.KeyApp

/**
 * rvOptions: item dac biet o dau + cac thumbnail anh.
 * clearable = true : [X clear] [random] [img1] [img2]...  headerCount = 2
 * clearable = false: [random] [img1] [img2]...             headerCount = 1
 *
 * ClearViewHolder va RandomViewHolder duoc tai su dung tu core/LayerActionViewHolders.kt
 */
class LayerAdapter(
    private val groupKey: String,
    val parts: String,
    val color: String?,
    val clearable: Boolean = true,
    private val onClear: () -> Unit,
    private val onRandom: () -> Unit,
    private val onClickIndex: (Int) -> Unit
) : ListAdapter<Int, RecyclerView.ViewHolder>(DIFF) {

    private var selectedIndex: Int? = null
    private var isCleared: Boolean = false

    // headerCount: clear (neu co) + random (luon co)
    private val headerCount get() = if (clearable) 2 else 1

    fun setSelected(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }

    fun setCleared(cleared: Boolean) {
        if (isCleared != cleared) {
            isCleared = cleared
            notifyDataSetChanged()
        }
    }

    // View types
    override fun getItemViewType(position: Int): Int = when {
        clearable && position == 0  -> TYPE_CLEAR
        clearable && position == 1  -> TYPE_RANDOM
        !clearable && position == 0 -> TYPE_RANDOM
        else                        -> TYPE_LAYER
    }

    override fun getItemCount(): Int = super.getItemCount() + headerCount

    // offset header khi lay item that tu list
    override fun getItem(position: Int): Int = super.getItem(position - headerCount)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CLEAR  -> ClearViewHolder(ItemLayerClearBinding.inflate(inflater, parent, false))
            TYPE_RANDOM -> RandomViewHolder(ItemLayerRandomBinding.inflate(inflater, parent, false))
            else        -> LayerViewHolder(ItemLayerBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ClearViewHolder  -> holder.bind(isCleared, onClear)
            is RandomViewHolder -> holder.bind(onRandom)
            is LayerViewHolder  -> holder.bind(getItem(position))
        }
    }

    inner class LayerViewHolder(
        private val binding: ItemLayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(index: Int) {
            Glide.with(binding.imageLayer.context)
                .load(KeyApp.getLayerUrl(groupKey, parts, index, color))
                .into(binding.imageLayer)

            binding.cardLayer.strokeWidth = if (!isCleared && index == selectedIndex) 4 else 0
            binding.root.setOnClickListener { onClickIndex(index) }
        }
    }

    companion object {
        const val TYPE_CLEAR  = 0
        const val TYPE_RANDOM = 1
        const val TYPE_LAYER  = 2

        val DIFF = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        }
    }
}
