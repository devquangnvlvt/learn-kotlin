package com.example.kotlinv4.ui.widget

/**
 * Các key chức năng của StickerView.
 * Truyền vào setFeatures() để bật/tắt từng chức năng.
 *
 * Ví dụ:
 *   // Bật tất cả
 *   stickerView.setFeatures(StickerFeature.ALL)
 *
 *   // Chỉ drag + scale
 *   stickerView.setFeatures(setOf(StickerFeature.DRAG, StickerFeature.SCALE))
 *
 *   // Chỉ xem, không tương tác
 *   stickerView.setFeatures(emptySet())
 */
object StickerFeature {
    const val DRAG   = "drag"    // di chuyển
    const val SCALE  = "scale"   // phóng to / thu nhỏ
    const val ROTATE = "rotate"  // xoay
    const val FLIP   = "flip"    // lật ngang
    const val DELETE = "delete"  // nút xóa góc trên trái — chạm vào tự remove khỏi parent

    /** Tất cả chức năng (không có DELETE — phải opt-in riêng) */
    val ALL = setOf(DRAG, SCALE, ROTATE, FLIP, DELETE)

    /** Tất cả + nút xóa */
    val ALL_WITH_DELETE = setOf(DRAG, SCALE, ROTATE, FLIP, DELETE)
}
