package com.example.kotlinv4.ui.detail

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.example.kotlinv4.R
import com.example.kotlinv4.databinding.ActivityDetailEditBinding
import com.example.kotlinv4.ui.base.BaseActivity
import com.example.kotlinv4.ui.successful.SuccessActivity
import com.example.kotlinv4.ui.widget.StickerFeature
import com.example.kotlinv4.ui.widget.StickerView

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

    private val viewModel: DetailEditViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater): ActivityDetailEditBinding =
        ActivityDetailEditBinding.inflate(inflater)

    private lateinit var stickerAdapter: StickerAdapter
    private lateinit var colorAdapter: ColorPickerColorAdapter
    private lateinit var imageAdapter: ColorPickerImageAdapter
    private lateinit var bubblesAdapter: BubblesAdapter

    // Danh sách các StickerView sticker đã add (không tính frame gốc)
    private val stickerViews = mutableListOf<StickerView>()

    // Container được tạo động để wrap stickerView — tránh sửa XML
    private lateinit var stickerContainer: android.widget.FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tải dữ liệu vào ViewModel
        viewModel.loadData(assets)

        // ── Frame gốc (bitmap character) ──────────────────────────────────────
        val path = intent.getStringExtra("FRAME_PATH")
        val bitmap = BitmapFactory.decodeFile(path)
        if (bitmap != null) {
            binding.stickerView.setFeatures(setOf(StickerFeature.DRAG, StickerFeature.SCALE, StickerFeature.FLIP))
            binding.stickerView.setBitmap(bitmap)
            binding.stickerView.onTouchedOutside = { deselectAllStickers() }
        }

        // ── Wrap stickerView trong FrameLayout để stack sticker overlay ────────
        val parent = binding.stickerView.parent as android.view.ViewGroup
        val index = parent.indexOfChild(binding.stickerView)
        val lp = binding.stickerView.layoutParams

        stickerContainer = android.widget.FrameLayout(this).apply {
            layoutParams = lp
            background = binding.stickerView.background
        }
        binding.stickerView.background = null
        binding.stickerView.layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        )
        parent.removeViewAt(index)
        stickerContainer.addView(binding.stickerView)
        parent.addView(stickerContainer, index)

        // Chạm vào vùng trống của container (không trúng StickerView nào) → deselect hết
        stickerContainer.setOnTouchListener { _, _ ->
            deselectAllStickers()
            binding.stickerView.deselect()
            false
        }

        // ── Color Picker Panel ────────────────────────────────────────────────
        val panel = binding.layoutColorPicker

        colorAdapter = ColorPickerColorAdapter { hex -> updateBackground(hex) }
        panel.rvColor.adapter = colorAdapter
        colorAdapter.submitList(viewModel.defaultColors)

        imageAdapter = ColorPickerImageAdapter { fileName -> updateBackground(fileName) }
        panel.rvImage.adapter = imageAdapter

        stickerAdapter = StickerAdapter { fileName -> updateSticker(fileName) }
        panel.rvSticker.adapter = stickerAdapter

        bubblesAdapter = BubblesAdapter { fileName -> showBubbleTextDialog(fileName) }
        panel.rvBubbles.adapter = bubblesAdapter

        // Observe StateFlow từ ViewModel để cập nhật adapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.backgroundList.collect { imageAdapter.submitList(it) }
                }
                launch {
                    viewModel.stickerList.collect { stickerAdapter.submitList(it) }
                }
                launch {
                    viewModel.bubblesList.collect { bubblesAdapter.submitList(it) }
                }
            }
        }

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

        fun setActiveIconTab(activeIndex: Int) {
            val icons = listOf(panel.iconTab1, panel.iconTab2, panel.iconTab3, panel.iconTab4)
            icons.forEachIndexed { i, icon ->
                icon.setBackgroundResource(
                    if (i == activeIndex) R.drawable.bg_icon_normal else R.drawable.bg_icon_active
                )
                icon.setColorFilter(
                    if (i == activeIndex)
                        Color.WHITE
                    else
                        Color.parseColor("#AAAAAA")
                )
            }
        }

        panel.iconTab1.setOnClickListener {
            setActiveIconTab(0)
            panel.rowBtn.visibility = View.VISIBLE
            panel.rvColor.visibility = View.VISIBLE
            panel.rvImage.visibility = View.GONE
            panel.rvSticker.visibility = View.GONE
            panel.rvBubbles.visibility = View.GONE
        }
        panel.iconTab2.setOnClickListener {
            setActiveIconTab(1)
            panel.rowBtn.visibility = View.GONE
            panel.rvSticker.visibility = View.VISIBLE
            panel.rvColor.visibility = View.GONE
            panel.rvImage.visibility = View.GONE
            panel.rvBubbles.visibility = View.GONE
        }
        panel.iconTab3.setOnClickListener {
            setActiveIconTab(2)
            panel.rowBtn.visibility = View.GONE
            panel.rvSticker.visibility = View.GONE
            panel.rvColor.visibility = View.GONE
            panel.rvImage.visibility = View.GONE
            panel.rvBubbles.visibility = View.VISIBLE
        }
        panel.iconTab4.setOnClickListener {
            setActiveIconTab(3)
            // TODO: nội dung tab 4
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnNext.setOnClickListener {
            deselectAllStickers()
            binding.stickerView.deselect()

            stickerContainer.post {
                val bitmap = captureFrame()
                val file = viewModel.saveFrameToCache(cacheDir, bitmap)

                val intent = Intent(this, SuccessActivity::class.java).apply {
                    putExtra("FRAME_PATH", file.absolutePath)
                }
                startActivity(intent)
            }
        }
    }

    // ── Capture ───────────────────────────────────────────────────────────────
    private fun captureFrame(): Bitmap {
        val container = stickerContainer
        val bitmap = Bitmap.createBitmap(container.width, container.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        container.draw(canvas)
        return bitmap
    }

    private fun addStickerView(bitmap: Bitmap) {
        deselectAllStickers()

        val newSticker = StickerView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            setFeatures(StickerFeature.ALL)
            setStickerBitmap(bitmap)
            onTouchedOutside = { deselectAllStickers() }
        }

        stickerContainer.addView(newSticker)
        stickerViews.add(newSticker)

        newSticker.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                stickerViews.remove(v as StickerView)
            }
        })
    }

    private fun deselectAllStickers() {
        stickerViews.forEach { it.deselect() }
    }

    // ── Background ────────────────────────────────────────────────────────────
    private fun updateBackground(value: String) {
        if (value.startsWith("file:///android_asset/")) {
            val assetPath = viewModel.cleanAssetPath(value)
            try {
                val inputStream = assets.open(assetPath)
                val drawable = Drawable.createFromStream(inputStream, null)
                stickerContainer.background = drawable
                imageAdapter.setSelected(value)
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
        val bitmap = viewModel.loadBitmapFromAsset(assets, value)
        if (bitmap != null) {
            addStickerView(bitmap)
            stickerAdapter.setSelected(value)
        }
    }

    // ── Bubbles Text Popup & Sticker Generation ──────────────────────────────
    private fun showBubbleTextDialog(value: String) {
        val bubbleBitmap = viewModel.loadBitmapFromAsset(assets, value) ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_bubble_text, null)
        val imgBubble = dialogView.findViewById<ImageView>(R.id.imgBubble)
        val edtBubbleText = dialogView.findViewById<EditText>(R.id.edtBubbleText)
        val dialogRoot = dialogView.findViewById<View>(R.id.dialogRoot)
        val bubbleContainer = dialogView.findViewById<View>(R.id.bubbleContainer)

        imgBubble.setImageBitmap(bubbleBitmap)

        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar).apply {
            setContentView(dialogView)
            window?.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        var isCommitted = false
        fun commitAndDismiss() {
            if (isCommitted) return
            isCommitted = true
            val text = edtBubbleText.text.toString().trim()
            val compositeBitmap = viewModel.drawTextOnBitmap(bubbleBitmap, text)
            addStickerView(compositeBitmap)
            bubblesAdapter.setSelected(value)
            dialog.dismiss()
        }

        dialogRoot.setOnClickListener { commitAndDismiss() }
        bubbleContainer.setOnClickListener { }

        dialog.show()
    }
}
