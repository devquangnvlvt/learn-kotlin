package com.example.kotlinv4.ui.successful

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.kotlinv4.databinding.ActivitySuccessfulBinding
import com.example.kotlinv4.ui.base.BaseActivity
import com.example.kotlinv4.ui.category.CategoryActivity
import java.io.OutputStream

class SuccessActivity : BaseActivity<ActivitySuccessfulBinding>() {

    override fun inflateBinding(inflater: LayoutInflater): ActivitySuccessfulBinding =
        ActivitySuccessfulBinding.inflate(inflater)

    private var resultBitmap: Bitmap? = null

    // Launcher xin permission WRITE_EXTERNAL_STORAGE (chỉ cần cho API < 29)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) saveToGallery() else Toast.makeText(this, "Cần quyền lưu ảnh", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra("FRAME_PATH")
        resultBitmap = BitmapFactory.decodeFile(path)
        resultBitmap?.let { binding.imageResult.setImageBitmap(it) }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        binding.btnSave.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // API 24–28: cần WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    saveToGallery()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                // API 29+: MediaStore không cần permission
                saveToGallery()
            }
        }
    }

    private fun saveToGallery() {
        val bmp = resultBitmap ?: run {
            Toast.makeText(this, "Không có ảnh để lưu", Toast.LENGTH_SHORT).show()
            return
        }

        val filename = "character_${System.currentTimeMillis()}.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KotlinV4")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri == null) {
            Toast.makeText(this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val stream: OutputStream? = contentResolver.openOutputStream(uri)
            stream?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }

            Toast.makeText(this, "Đã lưu vào thư viện ảnh", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            contentResolver.delete(uri, null, null)
            Toast.makeText(this, "Lưu ảnh thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
