package com.iarigo.spelling.ui.wordedit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.iarigo.spelling.R
import com.iarigo.spelling.databinding.DialogLetterSetBinding

/**
 * Задание возможных вариантов написания буквы в слове
 * Если заданы две буквы - нужно указать правильное написание
 * Если задана одна буква - слово будет учавствовать только в вариантах написания правильной буквы.
 *
 * По умолчанию букву надо вписать.
 *
 * Если указано возможное написание буквы - появляется возможность убрать написание этой буквы
 */

class DialogLetterSet: DialogFragment() {
    private lateinit var binding: DialogLetterSetBinding
    private var mCallback: OnLetterSetListener? = null // возвращаем в Activity, что было действие
    private var position: Int = 0 // порядковый номер буквы в слове
    private var letter: String = "" // буква
    private var secondLetter: String = "" // возможное написание буквы в слове
    private var oneLetter: Boolean = false // одна буква, нужно написать правильный вариант. По умолчанию нужно вписать правильную букву

    companion object {
        @JvmStatic
        fun newInstance(letter: String, position: Int, secondLetter: String, oneLetter: Boolean) = DialogLetterSet().apply {
            arguments = Bundle().apply {
                putInt("position", position)
                putString("letter", letter)
                putString("second_letter", secondLetter)
                putBoolean("one_letter", oneLetter)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getInt("position")?.let {
            position = it
        }
        arguments?.getString("letter")?.let {
            letter = it
        }
        arguments?.getString("second_letter")?.let {
            secondLetter = it
        }
        arguments?.getBoolean("one_letter")?.let {
            oneLetter = it
        }

        mCallback = if (context is OnLetterSetListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnSunCloudInsertedListener"
            )
        }
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        binding = DialogLetterSetBinding.inflate(LayoutInflater.from(requireContext()))

        // Буква, которую нужно написать правильно
        binding.letter.text = letter

        // Checkbox - написание буквы или выбор из 2-х вариантов
        binding.checkboxOneLetter.isChecked = oneLetter // устанавливаем значение
        binding.checkboxOneLetter.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutSecond.visibility = if (isChecked) View.INVISIBLE else View.VISIBLE // скрываем/показываем вторую букву
            oneLetter = isChecked // сохраняем состояние
        }

        // Второй варинат написания буквы в слове
        binding.layoutSecond.visibility = if (oneLetter) View.INVISIBLE else View.VISIBLE // скрываем/показываем вторую букву
        // если это ..., то не подставляем
        if (secondLetter != getText(R.string.word_write_letter))
            binding.letterSecond.setText(secondLetter)

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(binding.root)

        builder.setPositiveButton(R.string.word_edit_save) { _: DialogInterface?, _: Int ->
            // тут ничего не делаем, т.к. переопределили вызов
            // нужно, чтобы при нажатии кнопки окно не закрывалось в случае ошибки
        }

        // убрать вариант написания буквы
        builder.setNeutralButton(R.string.word_edit_remove) { _: DialogInterface?, _: Int ->
            val bundle = Bundle()
            bundle.putBoolean("remove", true)
            mCallback!!.onLetterSet(bundle)
            dismiss()
        }

        // отмена
        builder.setNegativeButton(R.string.word_edit_cancel) { _: DialogInterface?, _: Int ->
            dismiss()
        }
        return builder.create()
    }

    /**
     * Переопределили, чтобы закрывать окно самим
     */
    override fun onStart() {
        super.onStart() // super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { saveLetter() }
        }
    }

    /**
     * Сохраняем букву
     */
    private fun saveLetter() {
        if (checkSecondLetter()) {// проверяем, что вторая буква указана, если нужно
            val bundle = Bundle()
            bundle.putBoolean("remove", false)
            bundle.putInt("position", position)
            bundle.putString("second_letter", binding.letterSecond.text.toString())
            bundle.putBoolean("is_write", binding.checkboxOneLetter.isChecked)
            mCallback!!.onLetterSet(bundle)
            dismiss()
        }
    }

    /**
     * Проверяем, что вторая буква указана, если нужно
     */
    private fun checkSecondLetter(): Boolean {
        if (!binding.checkboxOneLetter.isChecked) { // указывается вторая буква
            if (binding.letterSecond.text.toString() == "") {// проверяем, что указана
                binding.letterSecond.error = requireContext().getString(R.string.word_edit_second_letter_error)
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    /**
     * Возвращение значения в Activity
     */
    interface OnLetterSetListener {
        fun onLetterSet(bundle: Bundle)
    }
}