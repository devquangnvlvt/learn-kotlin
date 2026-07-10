package com.example.kotlinv4.ui.api

import com.example.kotlinv4.ui.utils.KeyApp
import com.example.kotlinv4.ui.model.CategoryModel
import retrofit2.http.GET

interface ApiService {

    @GET(KeyApp.API_KEY)   // dùng lại hằng số path đã khai báo
    suspend fun getCharacterParts(): Map<String, List<CategoryModel>>
}