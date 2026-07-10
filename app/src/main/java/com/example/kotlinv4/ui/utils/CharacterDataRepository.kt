package com.example.kotlinv4.ui.utils

import com.example.kotlinv4.ui.model.CategoryWithGroup

/**
 * RAM cache tạm giữ toàn bộ data sau khi CategoryActivity gọi API xong.
 * DetailViewModel đọc từ đây trước, nếu trống thì fallback sang CategoryCacheManager (file).
 *
 * Vòng đời: tồn tại khi process còn sống.
 * Nếu Android kill process → allGroups = emptyList → DetailViewModel tự fallback file cache.
 */
object CharacterDataRepository {
    var allGroups: List<CategoryWithGroup> = emptyList()

    fun isReady() = allGroups.isNotEmpty()

    fun clear() {
        allGroups = emptyList()
    }
}
