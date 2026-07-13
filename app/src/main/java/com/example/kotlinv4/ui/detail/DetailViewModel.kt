package com.example.kotlinv4.ui.detail

import com.example.kotlinv4.ui.core.BaseMakerViewModel
import com.example.kotlinv4.ui.utils.CategoryCacheManager
import com.example.kotlinv4.ui.utils.CharacterDataRepository

/**
 * ViewModel của màn Detail.
 * Chỉ lo load data từ cache → tìm group → init engine.
 * Toàn bộ logic chọn tab/màu/index nằm trong BaseMakerViewModel + MakerEngine.
 */
class DetailViewModel(
    private val cacheManager: CategoryCacheManager
) : BaseMakerViewModel() {

    /**
     * Gọi 1 lần từ Activity.onCreate().
     *
     * Luồng lấy data:
     * 1. RAM cache (CharacterDataRepository) — nhanh nhất
     * 2. File cache (CategoryCacheManager)   — fallback khi process bị kill
     * 3. Không có gì → trả về false → Activity finish()
     */
    fun init(groupKey: String): Boolean {
        // Ưu tiên RAM, fallback file cache
        val allGroups = CharacterDataRepository.allGroups.ifEmpty {
            cacheManager.read().orEmpty().also {
                CharacterDataRepository.allGroups = it
            }
        }
            // không có data thì thoát
        if (allGroups.isEmpty()) return false

        val group = allGroups.find { it.groupKey == groupKey } ?: allGroups.first()

        // Khởi động engine với group data
        initEngine(group)
        return true
    }
}
