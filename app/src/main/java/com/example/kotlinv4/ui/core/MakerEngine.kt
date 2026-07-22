package com.example.kotlinv4.ui.core

import com.example.kotlinv4.ui.model.CategoryModel
import com.example.kotlinv4.ui.model.CategoryWithGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * MakerEngine — pure logic engine cho mọi loại character maker.
 *
 * Không biết gì về Android, Activity, View hay Glide.
 * Chỉ nhận data + action → tính toán → emit state mới.
 *
 * Cách dùng:
 * 1. Khởi tạo với group cần hiển thị
 * 2. Gọi selectVariant / selectColor / selectIndex khi user tương tác
 * 3. Collect state để cập nhật UI
 *
 * Maker mới chỉ cần:
 * - Tạo MakerEngine với data của mình
 * - Observe MakerState và vẽ UI theo layout riêng
 */
class MakerEngine(private val group: CategoryWithGroup) {

    // ── State ─────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(MakerState())
    val state: StateFlow<MakerState> = _state.asStateFlow()

    // ── Internal ──────────────────────────────────────────────────────────────
    /**
     * Lưu (color + imageIndex) của từng folder khi user rời đi.
     * Khôi phục khi quay lại → không mất lựa chọn cũ.
     */
    private val variantStateMap = mutableMapOf<String, SavedVariantState>()

    // Parts của tab đầu tiên — không có nút clear
    private var firstParts: String = ""

    // ── Init ──────────────────────────────────────────────────────────────────

    /**
     * Khởi tạo state đầu tiên từ group data.
     * Gọi 1 lần sau khi tạo engine.
     */
    fun init() {
        // Thứ tự layer: sort theo zIndex (số x trong "x-y") tăng dần
        // → zIndex nhỏ = layer dưới, zIndex lớn = layer trên
        val layerOrder = group.models
            .distinctBy { it.parts }
            .sortedBy { it.zIndex }
            .map { it.parts }

        // Thanh nav: sort theo navOrder (số y trong "x-y")
        val tabModels = group.models.sortedBy { it.navOrder }

        // Mặc định chọn tab đầu tiên
        val defaultModel = tabModels.firstOrNull()
        val defaultColor = defaultModel?.colors?.firstOrNull()

        // Ghi nhớ parts của tab đầu tiên để block nút clear
        firstParts = defaultModel?.parts ?: ""

        _state.update {
            it.copy(
                groupKey = group.groupKey,
                layerOrder = layerOrder,
                tabModels = tabModels,
                selectedModel = defaultModel,
                selectedColor = defaultColor,
                selectedImageIndex = 1,
                optionIndexList = buildIndexList(defaultModel),
                colorList = defaultModel?.colors.orEmpty(),
                isClearable = false, // tab đầu tiên không có nút clear
                renderEvent = defaultModel?.let { m ->
                    RenderEvent(parts = m.parts, imageIndex = 1, color = defaultColor)
                }
            )
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * User click 1 item trên thanh nav → chọn folder mới.
     * Tự động:
     * - Lưu state của folder cũ
     * - Khôi phục state đã lưu của folder mới (nếu có)
     * - Tự chọn màu đầu tiên nếu folder mới có màu và chưa từng chọn
     */
    fun selectVariant(model: CategoryModel) {
        val current = _state.value

        // Lưu state folder cũ
        current.selectedModel?.let { old ->
            variantStateMap[old.parts] = SavedVariantState(
                color = current.selectedColor,
                imageIndex = current.selectedImageIndex
            )
        }

        // Khôi phục hoặc dùng mặc định
        val saved = variantStateMap[model.parts]
        val color = saved?.color ?: model.colors.firstOrNull()
        val index = saved?.imageIndex ?: 1

        _state.update {
            it.copy(
                selectedModel = model,
                selectedColor = color,
                selectedImageIndex = index,
                optionIndexList = buildIndexList(model),
                colorList = model.colors,
                isClearable = model.parts != firstParts,
                clearedParts = it.clearedParts - model.parts,
                renderEvent = RenderEvent(parts = model.parts, imageIndex = index, color = color)
            )
        }
    }

    /**
     * User click thumbnail → chọn index ảnh.
     * Nếu layer đang bị cleared → tự động restore lại.
     */
    fun selectIndex(index: Int) {
        val model = _state.value.selectedModel ?: return
        _state.update {
            it.copy(
                selectedImageIndex = index,
                clearedParts = it.clearedParts - model.parts,
                renderEvent = RenderEvent(
                    parts = model.parts,
                    imageIndex = index,
                    color = it.selectedColor
                )
            )
        }
    }

    /**
     * Random chọn 1 index bất kỳ trong layer hiện tại.
     */
    fun randomIndex() {
        val model = _state.value.selectedModel ?: return
        val list = _state.value.optionIndexList
        if (list.isEmpty()) return
        val randomIdx = list.random()
        _state.update {
            it.copy(
                selectedImageIndex = randomIdx,
                clearedParts = it.clearedParts - model.parts,
                renderEvent = RenderEvent(
                    parts = model.parts,
                    imageIndex = randomIdx,
                    color = it.selectedColor
                )
            )
        }
    }

    /**
     * User bấm nút hủy → ẩn layer của parts hiện tại.
     * Nếu layer đang bị ẩn rồi thì không làm gì thêm.
     */
    fun clearLayer(parts: String) {
        val current = _state.value
        val newCleared = current.clearedParts + parts
        _state.update {
            it.copy(
                clearedParts = newCleared,
                renderEvent = null
            )
        }
    }

    /**
     * User click màu → đổi màu.
     * Rebuild optionIndexList vì thumbnail URL phụ thuộc màu.
     */
    fun selectColor(hex: String) {
        val model = _state.value.selectedModel ?: return
        val index = _state.value.selectedImageIndex
        _state.update {
            it.copy(
                selectedColor = hex,
                optionIndexList = buildIndexList(model),
                renderEvent = RenderEvent(parts = model.parts, imageIndex = index, color = hex)
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Tạo list [1..quantity], đảm bảo ít nhất 1 item */
    private fun buildIndexList(model: CategoryModel?): List<Int> =
        (1..(model?.quantityInt?.coerceAtLeast(1) ?: 1)).toList()
}

// ── Data classes ──────────────────────────────────────────────────────────────

/**
 * State dùng chung cho mọi maker.
 * UI của từng maker observe state này và vẽ theo layout riêng.
 */
data class MakerState(
    // groupKey để build URL ảnh
    val groupKey: String = "",

    // Thứ tự parts theo zIndex → Activity tạo ImageView đúng lớp
    val layerOrder: List<String> = emptyList(),

    // Data cho 3 row UI
    val tabModels: List<CategoryModel> = emptyList(),       // rvNavs
    val optionIndexList: List<Int> = emptyList(),           // rvOptions
    val colorList: List<String> = emptyList(),              // rvColors

    // Selection hiện tại
    val selectedModel: CategoryModel? = null,
    val selectedColor: String? = null,
    val selectedImageIndex: Int = 1,

    // Tab hiện tại có cho phép hủy layer không
    val isClearable: Boolean = true,

    // Trigger render ảnh — null = không cần render
    val renderEvent: RenderEvent? = null,

    // Parts đã bị user hủy chọn → ẩn layer
    val clearedParts: Set<String> = emptySet()
)

/**
 * Thông tin để render 1 layer.
 * Activity/Fragment dùng để build URL và load vào đúng ImageView.
 */
data class RenderEvent(
    val parts: String,
    val imageIndex: Int,
    val color: String?
)

/** State nội bộ, lưu lại khi user rời tab */
private data class SavedVariantState(
    val color: String?,
    val imageIndex: Int
)
