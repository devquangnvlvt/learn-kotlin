package com.example.kotlinv4.ui.model

data class CategoryWithGroup(
    val groupKey: String,
    val models: List<CategoryModel>
)

data class CategoryModel(
    val position: String,
    val parts: String,
    val colorArray: String,
    val quantity: String,
    val level: String,
    val clearable: Boolean = true   // false = parts bắt buộc, không cho ẩn
) {
    val colors: List<String>
        get() = if (colorArray.isBlank()) emptyList() else colorArray.split(",")

    val quantityInt: Int
        get() = quantity.toIntOrNull() ?: 0

    val levelInt: Int
        get() = level.toIntOrNull() ?: 0

    // parts = "x-y" => x = z-index (layer), y = thứ tự thanh nav (ĐÃ SỬA theo quy ước mới)
    val zIndex: Int
        get() = parts.substringBefore("-").toIntOrNull() ?: 0

    // sắp theo y (navOrder)
    val navOrder: Int
        get() = parts.substringAfter("-").toIntOrNull() ?: 0
}