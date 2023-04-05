package com.iarigo.spelling.ui.wordedit

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.iarigo.spelling.R
import com.iarigo.spelling.databinding.ActivityWordEditBinding
import com.iarigo.spelling.storage.entity.Letters
import com.iarigo.spelling.storage.entity.Words
import com.iarigo.spelling.ui.words.WordsActivity

/**
 * Добавление/редактирование слова.
 * TODO Нужно ли блокировать редактирование слова, если оно уже учавствовоало?
 * TODO тире в словах или пробел
 * TODO свободное написание
 *
 * При вводе слова, оно разбивается на буквы.
 * Буквы выводятся по n штук в ряд и могут занимать несколько рядов
 */

class WordEditActivity : AppCompatActivity(),
    DialogLetterSet.OnLetterSetListener {

    private lateinit var binding: ActivityWordEditBinding
    private var viewModel: WordEditViewModel? = null
    private var wordId: Long = 0 // редактирование слова

    private var LETTER_ROW = 0 // кол-во строк, которые уже используются
    private var LETTERS = 0 // кол-во букв, которые уже есть
    private var mLinearLayout: ViewGroup? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWordEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[WordEditViewModel::class.java]

        wordId = intent.getLongExtra("id", 0);
        if (wordId != 0L) { // редактирование слова
            viewModel!!.getWord(wordId) // выбираем слово
        }

        mLinearLayout = binding.letters

        // ввод слова
        binding.word.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                wordToLetter(s, before, count) // обрабатываем слово
            }
        })

        // Сохранить. Проверяем, чтобы была изменена хотя бы одна буква
        binding.save.setOnClickListener {
            if (binding.word.length() != 0) { // слово введено
                if (checkWord()) {// проверям, что добавлена хотя бы одна буква
                    saveWord(wordId) // Сохраняем
                } else { // ошибка, необходимо задать хотя бы одну букву
                    binding.word.error = getString(R.string.word_edit_save_error)
                }
            } else { // ошибка, необходимо ввести слово
                binding.word.error = getString(R.string.word_edit_one_letter)
            }
        }

        // Отмена
        binding.cancel.setOnClickListener {
            // finish()
            // или
            // возвращаемся на предыдущую
            val intent = Intent(applicationContext, WordsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        // Слово сохранили. Закрываем Activity
        viewModel!!.wordAdded.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                closeActivity(it)
            }
        }

        // Слово для редактирования выбрано. Заполняем
        viewModel!!.editWord.observe(this) { word ->
            word.getContentIfNotHandled()?.let {
                setWord(it)
            }
        }

        // Буквы слова
        viewModel!!.wordLetters.observe(this) { letters ->
            letters.getContentIfNotHandled()?.let {
                setWordLetters(it)
            }
        }
    }

    /**
     * Подставляем слово в строку ввода
     */
    private fun setWord(word: Words) {
        binding.word.setText(word.word)
    }

    /**
     * Подставляем пропущенные буквы
     */
    private fun setWordLetters(letters: List<Letters>) {
        for (letter in letters) {
            if (letter.missed) { // буква пропущена
                val bundle = Bundle()
                bundle.putBoolean("remove", false)
                bundle.putInt("position", letter.position)
                bundle.putString("second_letter", letter.letterOption)
                bundle.putBoolean("is_write", letter.onlyWrite)
                onLetterSet(bundle)
            }
        }
    }

    /**
     * Букву сохранили/отредактировали.
     * Закрываем Activity. Показываем сообщение, что сохранено
     */
    private fun closeActivity(close: Boolean) {
        // возвращаемся результат
        val intent = Intent()
        intent.putExtra("saved", true)
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * Изменилось слово.
     * Разбиваем на буквы
     * @param before - 1 - удалили символ
     *       0 - добавили
     *       или при удалении виртуальной клавиатурой - before больше, чем count
     * TODO проверка на максимальное кол-во символов в слове
     */
    private fun wordToLetter(s: CharSequence, before: Int, count: Int) {
        val word = s.toString().uppercase()
        val letters = word.toCharArray()

        if (before == 1 || before > count)  {
            removeLastLetter(letters.size + 1) // удаляем последний символ при необходимости. Стираем слово
        }

        // обнуляем
        LETTER_ROW = 0
        LETTERS = 0

        for(letterNumber in letters.indices) {
            LETTERS = letterNumber + 1
            updateLayout()// Открываем букву или строку с буквами

            // область с буквой
            val layoutLetter: LinearLayout = findViewById(getLayoutLetterIntName(LETTERS))
            layoutLetter.visibility = View.VISIBLE
            // клик по области
            layoutLetter.setOnClickListener {
                val position = getLetterPosition(it.id) // номер буквы
                var secondLetter = "" // вторая буква
                var oneLetter = true // выбор буквы или правильное написание
                // определяем есть установки для буквы или нет
                val layoutSmall: LinearLayout = findViewById(getLayoutSmallTextIntName(position))
                if (layoutSmall.visibility != View.GONE) {// редактирование
                    val textView: TextView = findViewById(getSmallTextIntName(position))
                    secondLetter = textView.text.toString()
                    oneLetter = textView.text == getText(R.string.word_write_letter) // буква или ...
                }
                val letter: TextView = findViewById(getTextIntName(position))
                // диалоговое окно с устанвкой
                val dialog: DialogFragment = DialogLetterSet.newInstance(
                    letter.text.toString(), position, secondLetter, oneLetter)
                dialog.show(supportFragmentManager, "dialogLetterSet")
            }

            // буква
            val textView: TextView = findViewById(getTextIntName(LETTERS))
            textView.text = letters[letterNumber].toString()
        }
    }

    /**
     * Добавляем Layout с партией букв
     */
    private fun addLettersLayout(layoutId: Int) {
        val layout: LinearLayout = findViewById(getLayoutIntName(layoutId))
        layout.visibility = View.VISIBLE
    }

    /**
     * Прячем Layout с партией букв
     */
    private fun hideLettersLayout(layoutId: Int) {
        val layout: LinearLayout = findViewById(getLayoutIntName(layoutId))
        layout.visibility = View.GONE
    }

    /**
     * Открываем букву или строку с буквами
     */
    private fun updateLayout() {
        if (LETTERS == 1) {// буква
            LETTER_ROW++
            addLettersLayout(LETTER_ROW)
        } else {
            if (LETTERS % 4 == 1) { // новая строка
                LETTER_ROW++
                addLettersLayout(LETTER_ROW)
            }
        }
    }

    /**
     * При необходимости удаляем последний символ. Стираем слово
     * @param letterCount - слово, которое напечатал поль-ль
     */
    private fun removeLastLetter(letterCount: Int) {
        // область с буквой
        val layoutLetter: LinearLayout = findViewById(getLayoutLetterIntName(LETTERS))
        layoutLetter.visibility = View.INVISIBLE

        if (letterCount % 4 == 1) { // скрываем строку, если буква была первой в строке
            hideLettersLayout(LETTER_ROW)
        }
    }

    /**
     * Результат указания буквы, которую необходимо вписать
     * Обновляем букву
     * или
     * Убираем варианты написания для этой буквы
     */
    override fun onLetterSet(bundle: Bundle) {
        val textLetter: TextView = findViewById(getTextIntName(bundle.getInt("position")))
        val layoutSmall: LinearLayout = findViewById(getLayoutSmallTextIntName(bundle.getInt("position")))
        if (bundle.getBoolean("remove")) {// убираем букву
            // меняем высоту правильной буквы
            val params: ViewGroup.LayoutParams = textLetter.layoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.letter_height)
            textLetter.layoutParams = params
            // размер текста
            textLetter.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.letter_text_size)
            )

            // видимость второго варианта написания
            layoutSmall.visibility = View.GONE
        } else { // устанавливаем значение
            // меняем высоту правильной буквы, чтобы поместился вариант написания
            val params: ViewGroup.LayoutParams = textLetter.layoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.letter_height_select)
            textLetter.layoutParams = params
            // размер текста
            textLetter.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.letter_text_size_select)
            )

            // видимость второго варианта написания
            layoutSmall.visibility = View.VISIBLE

            // второй вариант написания - буква или подставновка правильной (...)
            val textSecond: TextView = findViewById(getSmallTextIntName(bundle.getInt("position")))
            if (bundle.getBoolean(("is_write"), false)) { // ...
                textSecond.text = getText(R.string.word_write_letter)
            } else { // буква
                textSecond.text = bundle.getString("second_letter")
            }
        }
    }

    /**
     * Проверяем, что для слова указана хотя бы одна буква
     * @return true or false
     */
    private fun checkWord(): Boolean {
        val word = binding.word.text.toString().uppercase()
        val letters = word.toCharArray()

        for(letterNumber in letters.indices) {
            val position = letterNumber + 1
            val layoutSmall: LinearLayout = findViewById(getLayoutSmallTextIntName(position))
            if (layoutSmall.visibility == View.VISIBLE)
                return true
        }

        return false
    }

    /**
     * Сохраняем слово
     * @param wordId - если не 0 - редактирование слова
     */
    private fun saveWord(wordId: Long) {
        val word = binding.word.text.toString()
        // собираем буквы и варианты написания букв
        val letters = word.toCharArray()

        val missedLetters = MutableList(letters.size){ "" }

        for(letterNumber in letters.indices) {
            val position = letterNumber + 1
            val layoutSmall: LinearLayout = findViewById(getLayoutSmallTextIntName(position))
            if (layoutSmall.visibility == View.VISIBLE) { // эта буква задана
                val textView: TextView = findViewById(getSmallTextIntName(position))
                missedLetters.add(position, textView.text.toString())
            }
        }
        viewModel!!.saveWord(word, missedLetters, wordId)
    }

    /**
     * Определяем позицию буквы, по которой кликнули для редактирования
     */
    private fun getLetterPosition(id: Int): Int {
        return when(id) {
            R.id.layout_letter_1 -> 1
            R.id.layout_letter_2 -> 2
            R.id.layout_letter_3 -> 3
            R.id.layout_letter_4 -> 4
            R.id.layout_letter_5 -> 5
            R.id.layout_letter_6 -> 6
            R.id.layout_letter_7 -> 7
            R.id.layout_letter_8 -> 8
            R.id.layout_letter_9 -> 9
            R.id.layout_letter_10 -> 10
            R.id.layout_letter_11 -> 11
            R.id.layout_letter_12 -> 12
            R.id.layout_letter_13 -> 13
            R.id.layout_letter_14 -> 14
            R.id.layout_letter_15 -> 15
            R.id.layout_letter_16 -> 16
            R.id.layout_letter_17 -> 17
            R.id.layout_letter_18 -> 18
            R.id.layout_letter_19 -> 19
            R.id.layout_letter_20 -> 20
            else -> 0
        }
    }

    /**
     * Строки с буквами.
     * Сейчас их 5
     * Т.к. поиск по сгенерированному имени работает медленно с точки зрения android
     * вернем имя элемента по его порядковому номеру
     */
    private fun getLayoutIntName(number: Int): Int {
        return when (number) {
            1 -> R.id.letter_row_1
            2 -> R.id.letter_row_2
            3 -> R.id.letter_row_3
            4 -> R.id.letter_row_4
            5 -> R.id.letter_row_5
            else -> {
                R.id.letter_row_1
            }
        }
    }

    /**
     * Layout с буквами
     * layout_letter_1
     */
    private fun getLayoutLetterIntName(number: Int): Int {
        return when (number) {
            1 -> R.id.layout_letter_1
            2 -> R.id.layout_letter_2
            3 -> R.id.layout_letter_3
            4 -> R.id.layout_letter_4
            5 -> R.id.layout_letter_5
            6 -> R.id.layout_letter_6
            7 -> R.id.layout_letter_7
            8 -> R.id.layout_letter_8
            9 -> R.id.layout_letter_9
            10 -> R.id.layout_letter_10
            11 -> R.id.layout_letter_11
            12 -> R.id.layout_letter_12
            13 -> R.id.layout_letter_13
            14 -> R.id.layout_letter_14
            15 -> R.id.layout_letter_15
            16 -> R.id.layout_letter_16
            17 -> R.id.layout_letter_17
            18 -> R.id.layout_letter_18
            19 -> R.id.layout_letter_19
            20 -> R.id.layout_letter_20
            else -> R.id.layout_letter_1
        }
    }

    /**
     * Буквы.
     * Сейчас их 20
     * Т.к. поиск по сгенерированному имени работает медленно с точки зрения android
     * вернем имя элемента по его порядковому номеру
     */
    private fun getTextIntName(number: Int): Int {
        return when (number) {
            1 -> R.id.letter_1
            2 -> R.id.letter_2
            3 -> R.id.letter_3
            4 -> R.id.letter_4
            5 -> R.id.letter_5
            6 -> R.id.letter_6
            7 -> R.id.letter_7
            8 -> R.id.letter_8
            9 -> R.id.letter_9
            10 -> R.id.letter_10
            11 -> R.id.letter_11
            12 -> R.id.letter_12
            13 -> R.id.letter_13
            14 -> R.id.letter_14
            15 -> R.id.letter_15
            16 -> R.id.letter_16
            17 -> R.id.letter_17
            18 -> R.id.letter_18
            19 -> R.id.letter_19
            20 -> R.id.letter_20
            else -> {
                R.id.letter_1
            }
        }
    }

    /**
     * Область варианта написания буквы
     * layout_small_letter_1
     */
    private fun getLayoutSmallTextIntName(number: Int): Int {
        return when (number) {
            1 -> R.id.layout_small_letter_1
            2 -> R.id.layout_small_letter_2
            3 -> R.id.layout_small_letter_3
            4 -> R.id.layout_small_letter_4
            5 -> R.id.layout_small_letter_5
            6 -> R.id.layout_small_letter_6
            7 -> R.id.layout_small_letter_7
            8 -> R.id.layout_small_letter_8
            9 -> R.id.layout_small_letter_9
            10 -> R.id.layout_small_letter_10
            11 -> R.id.layout_small_letter_11
            12 -> R.id.layout_small_letter_12
            13 -> R.id.layout_small_letter_13
            14 -> R.id.layout_small_letter_14
            15 -> R.id.layout_small_letter_15
            16 -> R.id.layout_small_letter_16
            17 -> R.id.layout_small_letter_17
            18 -> R.id.layout_small_letter_18
            19 -> R.id.layout_small_letter_19
            20 -> R.id.layout_small_letter_20
            else -> R.id.layout_small_letter_1
        }
    }

    /**
     * Вариант написания буквы
     * text_var_letter_1
     */
    private fun getSmallTextIntName(number: Int): Int {
        return when (number) {
            1 -> R.id.text_var_letter_1
            2 -> R.id.text_var_letter_2
            3 -> R.id.text_var_letter_3
            4 -> R.id.text_var_letter_4
            5 -> R.id.text_var_letter_5
            6 -> R.id.text_var_letter_6
            7 -> R.id.text_var_letter_7
            8 -> R.id.text_var_letter_8
            9 -> R.id.text_var_letter_9
            10 -> R.id.text_var_letter_10
            11 -> R.id.text_var_letter_11
            12 -> R.id.text_var_letter_12
            13 -> R.id.text_var_letter_13
            14 -> R.id.text_var_letter_14
            15 -> R.id.text_var_letter_15
            16 -> R.id.text_var_letter_16
            17 -> R.id.text_var_letter_17
            18 -> R.id.text_var_letter_18
            19 -> R.id.text_var_letter_19
            20 -> R.id.text_var_letter_20
            else -> R.id.text_var_letter_1
        }
    }
}