package com.example.kotlinv4.ui.detail

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.lifecycle.ViewModel
import com.example.kotlinv4.ui.utils.loadAssets
import com.example.kotlinv4.ui.utils.loadBackgroundAssets
import com.example.kotlinv4.ui.utils.loadStickerAssets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class DetailEditViewModel : ViewModel() {

    val defaultColors = DetailEditActivity.DEFAULT_COLORS

    private val _backgroundList = MutableStateFlow<List<String>>(emptyList())
    val backgroundList: StateFlow<List<String>> = _backgroundList

    private val _stickerList = MutableStateFlow<List<String>>(emptyList())
    val stickerList: StateFlow<List<String>> = _stickerList

    private val _bubblesList = MutableStateFlow<List<String>>(emptyList())
    val bubblesList: StateFlow<List<String>> = _bubblesList

    fun loadData(assets: AssetManager) {
        _backgroundList.value = assets.loadBackgroundAssets()
        _stickerList.value = assets.loadStickerAssets()
        _bubblesList.value = assets.loadAssets("bubbles")
    }

    fun cleanAssetPath(value: String): String {
        return if (value.startsWith("file:///android_asset/")) {
            value.removePrefix("file:///android_asset/")
        } else {
            value
        }
    }

    fun loadBitmapFromAsset(assets: AssetManager, value: String): Bitmap? {
        val path = cleanAssetPath(value)
        return try {
            val inputStream = assets.open(path)
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Ghi chữ vào giữa bitmap bong bóng (Bubble)
     */
    fun drawTextOnBitmap(baseBitmap: Bitmap, text: String): Bitmap {
        val result = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        if (text.isEmpty()) return result

        val canvas = Canvas(result)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = (result.height * 0.10f).coerceAtLeast(24f)
            isFakeBoldText = true
        }

        val targetWidth = (result.width * 0.7f).toInt().coerceAtLeast(10)
        val staticLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, targetWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, textPaint, targetWidth, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)
        }

        val textHeight = staticLayout.height

        canvas.save()
        canvas.translate((result.width - targetWidth) / 2f, (result.height - textHeight) / 2f)
        staticLayout.draw(canvas)
        canvas.restore()

        return result
    }

    /**
     * Lưu frame bitmap đã capture vào cache file tạm
     */
    fun saveFrameToCache(cacheDir: File, bitmap: Bitmap): File {
        val file = File(cacheDir, "frame_temp.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }
}
