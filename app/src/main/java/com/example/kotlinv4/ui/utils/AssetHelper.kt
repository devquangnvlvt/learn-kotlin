package com.example.kotlinv4.ui.utils

import android.content.res.AssetManager

/**
 * Extension functions cho AssetManager — load danh sách file từ assets.
 * Dùng: assets.loadBackgroundAssets() / assets.loadStickerAssets()
 */

fun AssetManager.loadBackgroundAssets(): List<String> {
    return try {
        list("background")
            ?.filter { !it.endsWith(".db") }
            ?.map { "file:///android_asset/background/$it" }
            ?: emptyList()
    } catch (e: Exception) { emptyList() }
}

fun AssetManager.loadStickerAssets(): List<String> {
    return try {
        list("sticker")
            ?.filter { !it.endsWith(".db") }
            ?.map { "file:///android_asset/sticker/$it" }
            ?: emptyList()
    } catch (e: Exception) { emptyList() }
}

/**
 * Generic — load bất kỳ folder nào trong assets.
 * Dùng: assets.loadAssets("background") hoặc assets.loadAssets("sticker")
 */
fun AssetManager.loadAssets(folder: String): List<String> {
    return try {
        list(folder)
            ?.filter { !it.endsWith(".db") }
            ?.map { "file:///android_asset/$folder/$it" }
            ?: emptyList()
    } catch (e: Exception) { emptyList() }
}
