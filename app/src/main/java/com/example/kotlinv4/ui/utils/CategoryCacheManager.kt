package com.example.kotlinv4.ui.utils

import android.content.Context
import com.example.kotlinv4.ui.model.CategoryWithGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class CategoryCacheManager(context: Context) {
    private val gson = Gson()
    private val cacheFile = File(context.filesDir, "category_cache.json")

    fun save(data: List<CategoryWithGroup>) {
        try {
            val json = gson.toJson(data)
            cacheFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace() // ghi log lỗi nếu cần, không nên crash app vì lỗi cache
        }
    }

    fun read(): List<CategoryWithGroup>? {
        return try {
            if (!cacheFile.exists()) return null
            val json = cacheFile.readText()
            val type = object : TypeToken<List<CategoryWithGroup>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null // file lỗi/hỏng thì coi như không có cache
        }
    }

    fun clear() {
        if (cacheFile.exists()) cacheFile.delete()
    }
}