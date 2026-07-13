package com.example.kotlinv4.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.kotlinv4.ui.utils.NetworkHelper

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * Kiểm tra mạng — nếu không có thì hiển thị dialog và trả về false.
     * Dùng trước khi gọi API:
     *
     *   if (!checkNetwork()) return
     */
    protected fun checkNetwork(): Boolean {
        return if (NetworkHelper.isConnected(this)) {
            true
        } else {
            showNoNetworkDialog()
            false
        }
    }

    /**
     * Hiển thị modal thông báo không có mạng.
     * onRetry: callback khi user bấm "Thử lại" — mặc định null (chỉ đóng dialog).
     */
    protected fun showNoNetworkDialog(onRetry: (() -> Unit)? = null) {
        AlertDialog.Builder(this)
            .setTitle("Không có kết nối mạng")
            .setMessage("Vui lòng kiểm tra lại kết nối Internet và thử lại.")
            .setCancelable(false)
            .setPositiveButton("Thử lại") { dialog, _ ->
                dialog.dismiss()
                onRetry?.invoke()
            }
            .setNegativeButton("Đóng") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
