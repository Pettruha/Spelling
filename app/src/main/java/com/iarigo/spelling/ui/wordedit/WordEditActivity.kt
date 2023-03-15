package com.iarigo.spelling.ui.wordedit

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

    private val LETTER_BY_ROW = 4 // кол-во букв в строке
    private var LETTER_ROW = 0 // кол-во строк, которые уже используются
    private var LETTERS = 0 // кол-во букв, которые уже есть
    private var LAST_LETTERS_COUNT = 0 // кол-во букв, которые было
    private var mLinearLayout: ViewGroup? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWordEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[WordEditViewModel::class.java]

        mLinearLayout = binding.letters

        // ввод слова
        binding.word.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                wordToLetter(s, before) // обрабатываем слово
            }
        })

        // ТЕСТОВОЕ
        binding.layoutLetter1.setOnClickListener {
            var position = 1 // номер буквы
            var secondLetter = "" // вторая буква
            var oneLetter = true // выбор буквы или правильное написание
            // определяем есть установки для буквы или нет
            if (binding.layoutSmallLetter1.visibility != View.GONE) {// редактирование
                secondLetter = binding.textVarLetter1.text.toString()
                oneLetter = binding.textVarLetter1.text == getText(R.string.word_write_letter) // буква или ...
            }
            // диалоговое окно с устанвкой
            val dialog: DialogFragment = DialogLetterSet.newInstance(
                binding.textLetter1.text.toString(), 1, secondLetter, oneLetter)
            dialog.show(supportFragmentManager, "dialogLetterSet")
        }
    }

    /**
     * Изменилось слово.
     * Разбиваем на буквы
     * @param before - 1 - удалили символ
     *       0 - добавили
     * TODO проверка на максимальное кол-во символов в слове
     */
    private fun wordToLetter(s: CharSequence, before: Int) {
        val word = s.toString().uppercase()
        val letters = word.toCharArray()

        if (before == 1) {
            removeLastLetter(letters.size + 1) // удаляем последний символ при необходимости. Стираем слово
        }

        // обнуляем
        LETTER_ROW = 0
        LETTERS = 0

        for(letterNumber in letters.indices) {
            LETTERS = letterNumber + 1
            updateLayout()// Открываем букву или строку с буквами
            val textView: TextView = findViewById(getTextIntName(LETTERS))
            textView.visibility = View.VISIBLE
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
        val textView: TextView = findViewById(getTextIntName(letterCount))
        textView.visibility = View.INVISIBLE
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
        if (bundle.getBoolean("remove")) {// убираем букву
            // меняем высоту правильной буквы
            val params: ViewGroup.LayoutParams = binding.textLetter1.layoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.letter_height)
            binding.textLetter1.layoutParams = params
            // размер текста
            binding.textLetter1.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.letter_text_size)
            )

            // видимость второго варианта написания
            binding.layoutSmallLetter1.visibility = View.GONE
        } else { // устанавливаем значение
            // меняем высоту правильной буквы, чтобы поместился вариант написания
            val params: ViewGroup.LayoutParams = binding.textLetter1.layoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.letter_height_select)
            binding.textLetter1.layoutParams = params
            // размер текста
            binding.textLetter1.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.letter_text_size_select)
            )

            // видимость второго варианта написания
            binding.layoutSmallLetter1.visibility = View.VISIBLE

            // второй вариант написания - буква или подставновка правильной (...)
            if (bundle.getBoolean(("is_write"), false)) { // ...
                binding.textVarLetter1.text = getText(R.string.word_write_letter)
            } else { // буква
                binding.textVarLetter1.text = bundle.getString("second_letter")
            }
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
}