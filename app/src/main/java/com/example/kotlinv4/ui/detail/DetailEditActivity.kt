package com.example.kotlinv4.ui.detail

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.example.kotlinv4.R
import com.example.kotlinv4.databinding.ActivityDetailEditBinding
import com.example.kotlinv4.ui.base.BaseActivity
import com.example.kotlinv4.ui.widget.StickerFeature
import com.example.kotlinv4.ui.widget.StickerView
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.example.kotlinv4.ui.utils.loadBackgroundAssets
import com.example.kotlinv4.ui.utils.loadStickerAssets

class DetailEditActivity : BaseActivity<ActivityDetailEditBinding>() {

    companion object {
        val DEFAULT_COLORS = listOf(
            "EF9A9A", "F48FB1", "CE93D8", "B39DDB", "90CAF9",
            "80DEEA", "A5D6A7", "E6EE9C", "FFE082", "FFCC80",
            "FFAB91", "BCAAA4", "B0BEC5", "FFFFFF", "000000",
            "F8BBD0", "E1BEE7", "C5CAE9", "BBDEFB", "B2EBF2",
            "DCEDC8", "FFF9C4", "FFE0B2", "D7CCC8", "CFD8DC"
        )
    }

    override fun inflateBinding(inflater: LayoutInflater): ActivityDetailEditBinding =
        ActivityDetailEditBinding.inflate(inflater)

    private lateinit var stickerAdapter: StickerAdapter
    private lateinit var colorAdapter: ColorPickerColorAdapter
    private lateinit var imageAdapter: ColorPickerImageAdapter

    // Danh sách các StickerView sticker đã add (không tính frame gốc)
    private val stickerViews = mutableListOf<StickerView>()

    // Container được tạo động để wrap stickerView — tránh sửa XML
    private lateinit var stickerContainer: android.widget.FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Frame gốc (bitmap character) ──────────────────────────────────────
        val path = intent.getStringExtra("FRAME_PATH")
        val bitmap = BitmapFactory.decodeFile(path)
        if (bitmap != null) {
            binding.stickerView.setFeatures(setOf(StickerFeature.DRAG, StickerFeature.SCALE, StickerFeature.FLIP))
            binding.stickerView.setBitmap(bitmap)
            binding.stickerView.onTouchedOutside = { deselectAllStickers() }
        }

        // ── Wrap stickerView trong FrameLayout để stack sticker overlay ────────
        // Lấy parent (LinearLayout) và vị trí của stickerView trong đó
        val parent = binding.stickerView.parent as android.view.ViewGroup
        val index = parent.indexOfChild(binding.stickerView)
        val lp = binding.stickerView.layoutParams

        // Tạo FrameLayout container cùng LayoutParams với stickerView
        stickerContainer = android.widget.FrameLayout(this).apply {
            layoutParams = lp
            background = binding.stickerView.background
        }
        // Bỏ background gốc của stickerView (đã chuyển sang container)
        binding.stickerView.background = null
        // Reset layoutParams của stickerView thành MATCH_PARENT để fill container
        binding.stickerView.layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        )
        // Chuyển stickerView từ LinearLayout vào FrameLayout
        parent.removeViewAt(index)
        stickerContainer.addView(binding.stickerView)
        parent.addView(stickerContainer, index)

        // ── Color Picker Panel ────────────────────────────────────────────────
        val panel = binding.layoutColorPicker

        colorAdapter = ColorPickerColorAdapter { hex -> updateBackground(hex) }
        panel.rvColor.adapter = colorAdapter
        colorAdapter.submitList(DEFAULT_COLORS)

        imageAdapter = ColorPickerImageAdapter { fileName -> updateBackground(fileName) }
        panel.rvImage.adapter = imageAdapter
        imageAdapter.submitList(assets.loadBackgroundAssets())

        stickerAdapter = StickerAdapter { fileName -> updateSticker(fileName) }
        panel.rvSticker.adapter = stickerAdapter
        stickerAdapter.submitList(assets.loadStickerAssets())

        // ── Tab switching ─────────────────────────────────────────────────────
        panel.tabImage.setOnClickListener {
            panel.tabImage.setBackgroundResource(R.drawable.bg_tab_active)
            panel.tabColor.setBackgroundResource(R.drawable.bg_tab_inactive)
            panel.rvImage.visibility = View.VISIBLE
            panel.rvColor.visibility = View.GONE
        }
        panel.tabColor.setOnClickListener {
            panel.tabColor.setBackgroundResource(R.drawable.bg_tab_active)
            panel.tabImage.setBackgroundResource(R.drawable.bg_tab_inactive)
            panel.rvColor.visibility = View.VISIBLE
            panel.rvImage.visibility = View.GONE
        }

        // Helper: reset tất cả iconTab về normal, set cái được chọn thành active
        fun setActiveIconTab(activeIndex: Int) {
            val icons = listOf(panel.iconTab1, panel.iconTab2, panel.iconTab3, panel.iconTab4)
            icons.forEachIndexed { i, icon ->
                icon.setBackgroundResource(
                    if (i == activeIndex) R.drawable.bg_icon_normal else R.drawable.bg_icon_active
                )
                icon.setColorFilter(
                    if (i == activeIndex)
                        android.graphics.Color.WHITE
                    else
                        android.graphics.Color.parseColor("#AAAAAA")
                )
            }
        }

        panel.iconTab1.setOnClickListener {
            setActiveIconTab(0)
            panel.rowBtn.visibility = View.VISIBLE
            panel.rvColor.visibility = View.VISIBLE
            panel.rvImage.visibility = View.GONE
            panel.rvSticker.visibility = View.GONE
        }
        panel.iconTab2.setOnClickListener {
            setActiveIconTab(1)
            panel.rowBtn.visibility = View.GONE
            panel.rvSticker.visibility = View.VISIBLE
            panel.rvColor.visibility = View.GONE
            panel.rvImage.visibility = View.GONE
        }
        panel.iconTab3.setOnClickListener {
            setActiveIconTab(2)
            // TODO: nội dung tab 3
        }
        panel.iconTab4.setOnClickListener {
            setActiveIconTab(3)
            // TODO: nội dung tab 4
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    // ── Add sticker overlay ───────────────────────────────────────────────────

    /**
     * Tạo 1 StickerView mới, set bitmap sticker, add vào stickerContainer.
     * Deselect tất cả sticker cũ trước khi add cái mới.
     */
    private fun addStickerView(bitmap: Bitmap) {
        deselectAllStickers()

        val newSticker = StickerView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            // Sticker overlay: tất cả features + nút xóa
            setFeatures(StickerFeature.ALL)
            setStickerBitmap(bitmap)
            onTouchedOutside = { deselectAllStickers() }
        }

        stickerContainer.addView(newSticker)
        stickerViews.add(newSticker)

        // Khi sticker bị xóa khỏi parent (qua handle DELETE) → sync list
        newSticker.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                stickerViews.remove(v as StickerView)
            }
        })
    }

    /** Deselect tất cả StickerView overlay (không ảnh hưởng frame gốc) */
    private fun deselectAllStickers() {
        stickerViews.forEach { it.deselect() }
    }

    // ── Background ────────────────────────────────────────────────────────────

    private fun updateBackground(value: String) {
        if (value.startsWith("file:///android_asset/")) {
            val assetPath = value.removePrefix("file:///android_asset/")
            try {
                val inputStream = assets.open(assetPath)
                val drawable = Drawable.createFromStream(inputStream, null)
                stickerContainer.background = drawable
                imageAdapter.setSelected(assetPath)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            stickerContainer.setBackgroundColor(Color.parseColor("#$value"))
            colorAdapter.setSelected(value)
        }
    }

    // ── Sticker ───────────────────────────────────────────────────────────────

    private fun updateSticker(value: String) {
        if (value.startsWith("file:///android_asset/")) {
            val assetPath = value.removePrefix("file:///android_asset/")
            try {
                val inputStream = assets.open(assetPath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (bitmap != null) {
                    addStickerView(bitmap)
                    stickerAdapter.setSelected(assetPath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
