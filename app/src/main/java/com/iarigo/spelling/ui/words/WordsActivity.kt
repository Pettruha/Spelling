package com.iarigo.spelling.ui.words

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.spelling.R
import com.iarigo.spelling.databinding.ActivityWordsBinding
import com.iarigo.spelling.storage.entity.Words
import com.iarigo.spelling.ui.wordedit.WordEditActivity

/**
 * Список слов, которые существуют в приложении.
 */

class WordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWordsBinding
    private var viewModel: WordsViewModel? = null
    private var mAdapter: WordsAdapter? = null
    private val aList = ArrayList<Words>() // Список слов

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[WordsViewModel::class.java]

        binding.add.setOnClickListener { addNewWord() } // добавление нового слова

        registryWordList() // регистрируем адаптер

        // Сформировали список слов. Обновляем адаптер
        viewModel!!.statObservable.observe(this) { wordList ->
            updateWordList(wordList)
        }

        // Стрелочка назад
        binding.backArrow.setOnClickListener {
            // возвращаемся результат
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    /**
     * возобновление работы приложения
     */
    override fun onResume() {
        super.onResume()
        getWordList() // получаем список
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
        result: ActivityResult? ->
            val newListId = result!!.data!!.getLongExtra("listId", 0L)
            // если возвращаемся после сохранения/редактирования слова - выводим сообщение
            val intent = intent
            if (intent.getBooleanExtra("saved", false)) {
                getWordList() // обновляем список
                Toast.makeText(applicationContext, R.string.word_saved, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    /**
     * Получаем список слов
     */
    private fun getWordList() {
        viewModel!!.wordList.value = Bundle()
    }

    /**
     * Регистрируем список
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun registryWordList() {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding.listWord.setHasFixedSize(true) // без этого перемещает

        // use a linear layout manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.listWord.layoutManager = layoutManager

        // specify an adapter (see also next example)
        mAdapter = WordsAdapter(aList)

        // устанавливаем значения
        binding.listWord.adapter = mAdapter

        // разделитель между элементами
        // Get drawable object
        val mDivider = ContextCompat.getDrawable(this, R.drawable.word_list_divider)
        // Create a DividerItemDecoration whose orientation is Horizontal
        val hItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        // Set the drawable on it
        hItemDecoration.setDrawable(mDivider!!)
        binding.listWord.addItemDecoration(hItemDecoration) // устанавливаем значение

        // клик по элементу
        mAdapter!!.onItemClick = { item ->
            // открываем на редактирование
            val intent = Intent(this, WordEditActivity::class.java)
            intent.putExtra("id", item.id)
            wordEditLauncher.launch(intent)
        }

        // поиск по списку слов
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(msg: String): Boolean {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                filter(msg)
                return false
            }
        })
    }

    /**
     * Обновляем список
     * Если поль-ль авторизован, то показываем все его списки
     * Если поль-ль не авторизован - показываем только его локальные списки
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateWordList(words: List<Words>) {
        aList.clear() // обнуляем список элементов
        aList.addAll(words)
        /*
        for (i in words.indices) {
            val word = words[i]
            val hm = HashMap<String, String>()
            hm["Id"] = word.id.toString() // id списка
            hm["name"] = word.word // название
            hm["system"] = word.system.toString() // дата обновления
            aList.add(hm) // добавляем в список
        }

         */
        mAdapter!!.notifyDataSetChanged() // обновляем список
    }

    /**
     * Фильтр по слову
     */
    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredList: ArrayList<Words> = ArrayList()

        // running a for loop to compare elements.
        for (item in aList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.word.lowercase().contains(text.lowercase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, getString(R.string.words_search_nothing), Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            mAdapter!!.filterList(filteredList)
        }
    }
}