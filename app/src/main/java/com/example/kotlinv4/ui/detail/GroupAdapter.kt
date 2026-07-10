package com.example.kotlinv4.ui.detail

import com.bumptech.glide.Glide
import com.example.kotlinv4.databinding.ItemTabBinding
import com.example.kotlinv4.ui.base.BaseAdapter
import com.example.kotlinv4.ui.model.CategoryWithGroup
import com.example.kotlinv4.ui.utils.KeyApp

/**
 * rvTabs: chọn NHÓM (áo/tóc/mắt...). Chỉ đổi nhóm đang browse ở rvOptions,
 * KHÔNG xóa/ảnh hưởng layer của các nhóm khác đã chọn trước đó.
 */
class GroupAdapter(
    private val onClickGroup: (CategoryWithGroup) -> Unit
) : BaseAdapter<CategoryWithGroup, ItemTabBinding>(ItemTabBinding::inflate) {

    private var activeGroupKey: String? = null

    fun setActiveGroup(groupKey: String) {
        activeGroupKey = groupKey
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemTabBinding, item: CategoryWithGroup, position: Int) {
        val firstModel = item.models.minByOrNull { it.navOrder }
        firstModel?.let {
            Glide.with(binding.imageTab.context)
                .load(KeyApp.getNavUrl(item.groupKey, it.parts))
                .into(binding.imageTab)
        }

        binding.cardTab.strokeWidth = if (item.groupKey == activeGroupKey) 4 else 0
        binding.root.setOnClickListener { onClickGroup(item) }
    }
}