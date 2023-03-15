package com.iarigo.spelling

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import com.iarigo.spelling.databinding.ActivityMainBinding
import com.iarigo.spelling.ui.game.GameActivity
import com.iarigo.spelling.ui.words.WordsActivity

/**
 * Выбор правильной буквы для слова или написание нужной буквы самостоятельно
 * Карточки меняются, нужно указать правильное написание слова
 *
 * Два режима работы:
 * 1. Для слова задана буква, которая может быть вместо правильной буквы. Такое слово может учавствовать в написании правильной буквы и выборе из двух букв.
 * 2. Для слова указано, что буква пропущена. Такое слово учавтсвет только в написании правльной буквы.
 *
 * Система оценок.
 * 1. Оценка как в школе. Можно указать строгость оценки. Использование минусов.
 * 2. Фигурки животных. Если все неправильно - драный кот в подарок.
 *
 * Родитель может установить пин-код на настройки системы оценок
 */

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
/*
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
 */

        binding.layout.start.setOnClickListener{
            val intent = Intent(this, GameActivity::class.java)
            gameLauncher.launch(intent)
            overridePendingTransition(R.anim.left_out, R.anim.right_in) // стиль перехода к Activity
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_words -> { // список слов
                val intent = Intent(this, WordsActivity::class.java)
                activityWordsLauncher.launch(intent)
            }
        }
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_words -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /**
     * Результат WordsActivity
     */
    private var activityWordsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

    }

    /**
     * Результат игры
     */
    private var gameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

    }
}