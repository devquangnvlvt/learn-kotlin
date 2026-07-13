package com.example.kotlinv4.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.kotlinv4.R
import com.example.kotlinv4.databinding.ActivityDetailBinding
import com.example.kotlinv4.ui.base.BaseActivity
import com.example.kotlinv4.ui.core.MakerState
import com.example.kotlinv4.ui.core.RenderEvent
import com.example.kotlinv4.ui.utils.CategoryCacheManager
import com.example.kotlinv4.ui.utils.KeyApp
import kotlinx.coroutines.launch

/**
 * DetailActivity chỉ lo UI:
 * - Observe MakerState từ ViewModel → cập nhật 3 RV + render layer
 * - Forward user action lên ViewModel
 * - Tạo ImageView theo layerOrder
 *
 * Logic hoàn toàn nằm trong MakerEngine, Activity không giữ state nghiệp vụ.
 */
class DetailActivity : BaseActivity<ActivityDetailBinding>() {

    private val viewModel: DetailViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DetailViewModel(CategoryCacheManager(applicationContext)) as T
            }
        }
    }

    // parts → ImageView trong frameCharacter
    private val layerViewMap = mutableMapOf<String, ImageView>()

    private lateinit var navAdapter: NavAdapter

    private var layerAdapter: LayerAdapter? = null
    private var colorAdapter: ColorAdapter? = null

    override fun inflateBinding(inflater: LayoutInflater): ActivityDetailBinding =
        ActivityDetailBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val groupKey = intent.getStringExtra("SELECTED_KEY") ?: ""

        // init trả false = không có data → quay về màn trước
        if (!viewModel.init(groupKey)) { finish(); return }

        setupAdapters()
        observeState()

        binding.btnBack.setOnClickListener { finish() }
    }

    // ── Setup adapters (1 lần) ────────────────────────────────────────────────
    private fun setupAdapters() {
        // LayoutManager đã set trong XML
        navAdapter = NavAdapter { model -> viewModel.onSelectVariant(model) }
        binding.rvNavs.adapter = navAdapter
    }

    // ── Observe ───────────────────────────────────────────────────────────────
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.makerState.collect { state ->
                buildLayerViewsIfNeeded(state.layerOrder)
                updateNavRow(state)
                updateOptionsRow(state)
                updateColorsRow(state)
                applyLayerVisibility(state.clearedParts)
                state.renderEvent?.let { renderLayer(state.groupKey, it) }
            }
        }
    }

    // ── Update từng row ───────────────────────────────────────────────────────

    private fun updateNavRow(state: MakerState) {
        navAdapter.submitList(state.tabModels)
        state.selectedModel?.let { navAdapter.setSelected(it.parts) }
    }

    private fun updateOptionsRow(state: MakerState) {
        val parts = state.selectedModel?.parts ?: ""
        val color = state.selectedColor
        val isCleared = state.clearedParts.contains(parts)

        val needRebuild = layerAdapter == null
                || layerAdapter?.parts != parts
                || layerAdapter?.color != color
                || layerAdapter?.clearable != state.isClearable

        if (needRebuild) {
            layerAdapter = LayerAdapter(
                groupKey = state.groupKey,
                parts = parts,
                color = color,
                clearable = state.isClearable,
                onClear = {
                    val currentlyCleared = viewModel.makerState.value.clearedParts.contains(parts)
                    if (currentlyCleared) {
                        viewModel.makerState.value.selectedModel?.let { viewModel.onSelectVariant(it) }
                    } else {
                        viewModel.onClearLayer(parts)
                    }
                }
            ) { index -> viewModel.onSelectIndex(index) }
            binding.rvOptions.adapter = layerAdapter
            layerAdapter?.submitList(state.optionIndexList)
        }

        layerAdapter?.setSelected(state.selectedImageIndex)
        layerAdapter?.setCleared(isCleared)
    }

    private fun updateColorsRow(state: MakerState) {
        if (state.colorList.isEmpty()) {
            binding.rvColors.visibility = View.INVISIBLE
            return
        }
        binding.rvColors.visibility = View.VISIBLE
        colorAdapter = ColorAdapter { hex -> viewModel.onSelectColor(hex) }
        binding.rvColors.adapter = colorAdapter
        colorAdapter?.submitList(state.colorList)
        state.selectedColor?.let { colorAdapter?.setSelected(it) }
    }

    // ── Build layer ImageViews (1 lần) ────────────────────────────────────────
    private fun buildLayerViewsIfNeeded(layerOrder: List<String>) {
        if (layerViewMap.isNotEmpty() || layerOrder.isEmpty()) return
        for (parts in layerOrder) {
            val iv = ImageView(this).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            binding.frameCharacter.addView(iv)
            layerViewMap[parts] = iv
        }
    }

    // ── Apply layer visibility ─────────────────────────────────────────────────
    private fun applyLayerVisibility(clearedParts: Set<String>) {
        layerViewMap.forEach { (parts, iv) ->
            iv.visibility = if (parts in clearedParts) View.INVISIBLE else View.VISIBLE
        }
    }

    // ── Render layer ──────────────────────────────────────────────────────────
    private fun renderLayer(groupKey: String, event: RenderEvent) {
        val iv = layerViewMap[event.parts] ?: return
        Glide.with(iv.context)
            .load(KeyApp.getLayerUrl(groupKey, event.parts, event.imageIndex, event.color))
            .placeholder(iv.drawable)
            .error(R.drawable.ic_error)
            .dontAnimate()
            .into(iv)
    }
}
