package com.iarigo.spelling.ui.game

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.iarigo.spelling.databinding.ActivityGameBinding

/**
 * Игровой запуск. Выбор типа игры
 * - вписать букву
 * - выбрать правильную букву
 */
class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding
    private var viewModel: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        binding.first.setOnClickListener { // вписать букву

        }

        binding.second.setOnClickListener {// выбрать правильную букву

        }
    }
}