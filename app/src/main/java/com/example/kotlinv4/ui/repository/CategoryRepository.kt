package com.example.kotlinv4.ui.repository


import com.example.kotlinv4.ui.utils.Resource
import com.example.kotlinv4.ui.api.RetrofitClient
import com.example.kotlinv4.ui.model.CategoryWithGroup
import retrofit2.HttpException
import java.io.IOException
import kotlin.collections.component1
import kotlin.collections.component2

class CategoryRepository {

    suspend fun getParts(): Resource<List<CategoryWithGroup>> {
        return try {
            val response = RetrofitClient.apiService.getCharacterParts()

            val result = response
                .mapNotNull { (key, list) ->
                    val first = list.firstOrNull() ?: return@mapNotNull null
                    CategoryWithGroup(groupKey = key, models = list)
                }
                .sortedBy {  it.models.firstOrNull()?.levelInt ?: 0 }

            Resource.Success(result)
        } catch (e: IOException) {
            Resource.Error("Không có kết nối mạng")
        } catch (e: HttpException) {
            Resource.Error("Lỗi server: ${e.code()}")
        }
    }
}