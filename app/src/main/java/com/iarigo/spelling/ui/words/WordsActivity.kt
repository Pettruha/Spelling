package com.iarigo.spelling.ui.words

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.iarigo.spelling.databinding.ActivityWordsBinding
import com.iarigo.spelling.ui.wordedit.WordEditActivity

/**
 * Список слов, которые существуют в приложении.
 */

class WordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWordsBinding
    private var viewModel: WordsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[WordsViewModel::class.java]

        binding.add.setOnClickListener { addNewWord() } // добавление нового слова
    }

    /**
     * Добавление нового слова
     */
    private fun addNewWord() {
        val intent = Intent(this, WordEditActivity::class.java)
        wordEditLauncher.launch(intent)
    }

    /**
     * Результат добавления слова
     */
    private var wordEditLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

    }
}