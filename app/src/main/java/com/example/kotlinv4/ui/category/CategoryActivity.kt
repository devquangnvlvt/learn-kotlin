package com.example.kotlinv4.ui.category

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.example.kotlinv4.ui.base.BaseActivity
import com.example.kotlinv4.databinding.ActivityCategoryBinding
import com.example.kotlinv4.databinding.ActivityMainBinding
import com.example.kotlinv4.ui.detail.DetailActivity
import com.example.kotlinv4.ui.home.MainActivity
import com.example.kotlinv4.ui.repository.CategoryRepository
import com.example.kotlinv4.ui.utils.CategoryCacheManager
import com.example.kotlinv4.ui.utils.Resource
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.jvm.java

class CategoryActivity: BaseActivity<ActivityCategoryBinding>() {

    private lateinit var adapter: CategoryAdapter

    private val viewModel: CategoryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CategoryViewModel(
                    CategoryRepository(),           // cần đúng constructor thật của bạn
                    CategoryCacheManager(applicationContext)
                ) as T
            }
        }
    }


    override  fun inflateBinding(inflater: LayoutInflater): ActivityCategoryBinding{
        return ActivityCategoryBinding.inflate(inflater)
    }
    private fun setupRecyclerView() {
        adapter = CategoryAdapter{ CategoryWithGroup ->
            viewModel.getData(CategoryWithGroup)
        }
       // binding.recyclerView.layoutManager = LinearLayoutManager(this)
       // binding.recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 = số cột
        binding.recyclerView.adapter = adapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupRecyclerView()
        observeParts()
        observeNavigation()   // thêm dòng này
        viewModel.preloadData()

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun observeNavigation() {
        lifecycleScope.launch {
            viewModel.navigateToDetail.collect { groupKey ->
                val intent = Intent(this@CategoryActivity, DetailActivity::class.java).apply {
                    putExtra("SELECTED_KEY", groupKey)
                }
                startActivity(intent)
            }
        }
    }

    private fun observeParts() {
        lifecycleScope.launch {
            viewModel.categoryRepository.collect { state ->
                when (state) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                        binding.tvError.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.tvError.visibility = View.GONE
                        adapter.submitList(state.data)
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = state.message
                    }
                }
            }
        }
    }
}