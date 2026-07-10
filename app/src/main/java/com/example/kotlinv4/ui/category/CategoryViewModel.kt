package com.example.kotlinv4.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinv4.ui.model.CategoryWithGroup
import com.example.kotlinv4.ui.repository.CategoryRepository
import com.example.kotlinv4.ui.utils.CategoryCacheManager
import com.example.kotlinv4.ui.utils.CharacterDataRepository
import com.example.kotlinv4.ui.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val cacheManager: CategoryCacheManager
) : ViewModel() {

    private val _categoryRepository = MutableStateFlow<Resource<List<CategoryWithGroup>>>(Resource.Loading)
    val categoryRepository: StateFlow<Resource<List<CategoryWithGroup>>> = _categoryRepository

    // Sự kiện điều hướng — Activity sẽ lắng nghe để mở DetailActivity
    private val _navigateToDetail = Channel<String>(Channel.BUFFERED)
    val navigateToDetail = _navigateToDetail.receiveAsFlow()

    fun getData(data: CategoryWithGroup) {
        // Lưu toàn bộ list hiện có (đã load từ preloadData) vào RAM cache cho DetailActivity đọc lại
        val currentState = _categoryRepository.value
        if (currentState is Resource.Success) {
            CharacterDataRepository.allGroups = currentState.data
        }

        // Bắn sự kiện điều hướng, kèm groupKey của item vừa bấm
        viewModelScope.launch {
            _navigateToDetail.send(data.groupKey)
        }
    }

    fun preloadData() {
        val cached = cacheManager.read()
        if (!cached.isNullOrEmpty()) {
            _categoryRepository.value = Resource.Success(cached)
        }

        viewModelScope.launch {
            val result = repository.getParts()

            if (result is Resource.Success) {
                cacheManager.save(result.data)
            }

            if (result is Resource.Success || cached.isNullOrEmpty()) {
                _categoryRepository.value = result // xong thì cập nhật UI
            }
        }
    }
}