package com.example.kotlinv4.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater

import com.example.kotlinv4.ui.base.BaseActivity
import com.example.kotlinv4.databinding.ActivityMainBinding
import com.example.kotlinv4.ui.category.CategoryActivity

class MainActivity: BaseActivity<ActivityMainBinding>()  {

    override  fun inflateBinding(inflater: LayoutInflater): ActivityMainBinding{
        return ActivityMainBinding.inflate(inflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnApi.setOnClickListener {
            if (!checkNetwork()) return@setOnClickListener
            startActivity(Intent(this, CategoryActivity::class.java))
        }
    }
}