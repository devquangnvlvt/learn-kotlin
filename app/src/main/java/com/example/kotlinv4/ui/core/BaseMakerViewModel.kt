package com.example.kotlinv4.ui.core

import androidx.lifecycle.ViewModel
import com.example.kotlinv4.ui.model.CategoryModel
import com.example.kotlinv4.ui.model.CategoryWithGroup
import kotlinx.coroutines.flow.StateFlow

/**
 * Base ViewModel dùng chung cho mọi maker.
 *
 * Giữ MakerEngine và expose state + action ra ngoài.
 * Subclass chỉ cần cung cấp group data bằng cách gọi initEngine().
 *
 * Ví dụ maker mới:
 * class Maker2ViewModel(cache: CacheManager) : BaseMakerViewModel() {
 *     fun init(groupKey: String) {
 *         val group = loadGroup(groupKey)
 *         initEngine(group)
 *     }
 * }
 */
abstract class BaseMakerViewModel : ViewModel() {

    // Engine được khởi tạo lazy khi subclass gọi initEngine()
    private lateinit var engine: MakerEngine

    // Expose state từ engine ra cho Activity/Fragment observe
    val makerState: StateFlow<MakerState>
        get() = engine.state

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Subclass gọi hàm này sau khi đã có group data.
     * Engine sẽ tự init state đầu tiên.
     */
    protected fun initEngine(group: CategoryWithGroup) {
        engine = MakerEngine(group)
        engine.init()
    }

    // ── Forward actions xuống engine ──────────────────────────────────────────

    /** User click tab trên rvNavs */
    fun onSelectVariant(model: CategoryModel) = engine.selectVariant(model)

    /** User click thumbnail trên rvOptions */
    fun onSelectIndex(index: Int) = engine.selectIndex(index)

    /** User click màu trên rvColors */
    fun onSelectColor(hex: String) = engine.selectColor(hex)

    /** User bấm nút hủy → ẩn layer hiện tại */
    fun onClearLayer(parts: String) = engine.clearLayer(parts)

    /** Kiểm tra engine đã init chưa (tránh crash khi gọi trước initEngine) */
    fun isReady(): Boolean = ::engine.isInitialized
}
