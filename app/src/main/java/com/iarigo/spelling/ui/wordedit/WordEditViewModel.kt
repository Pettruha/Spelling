package com.iarigo.spelling.ui.wordedit

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iarigo.spelling.R
import com.iarigo.spelling.helper.Event
import com.iarigo.spelling.repository.LettersRepository
import com.iarigo.spelling.repository.WordRepository
import com.iarigo.spelling.storage.entity.Letters
import com.iarigo.spelling.storage.entity.Words
import kotlinx.coroutines.launch

class WordEditViewModel(application: Application): AndroidViewModel(application) {

    private var wordRepository: WordRepository = WordRepository(application)
    private var lettersRepository: LettersRepository = LettersRepository(application)
    @SuppressLint("StaticFieldLeak")
    private var context: Context = application.applicationContext

    // close WordEditActivity
    private val _wordAdded = MutableLiveData<Event<Boolean>>()
    val wordAdded: LiveData<Event<Boolean>>
        get() = _wordAdded

    // выбираем слово для редактирования
    private val _editWord = MutableLiveData<Event<Words>>()
    val editWord: LiveData<Event<Words>>
        get() = _editWord

    // выбираем буквы
    private val _wordLetters = MutableLiveData<Event<List<Letters>>>()
    val wordLetters: LiveData<Event<List<Letters>>>
        get() = _wordLetters

    /**
     * Выбираем слово для редактирования
     * и
     * пропущенные буквы
     */
    fun getWord(id: Long) {
        // слово
        viewModelScope.launch {
            _editWord.value = Event(wordRepository.getWord(id)!!)
        }

        // буквы
        viewModelScope.launch {
            _wordLetters.value = Event(lettersRepository.getLetters(id)!!)
        }
    }

    /**
     * Сохраняем слово, буквы и варианты написания букв
     * @param wordId - если не 0 - редактирование слова
     */
    fun saveWord(word: String, missedLetters: MutableList<String>, wordId: Long) = viewModelScope.launch {
        var newWordId = wordId;
        if (wordId != 0L) { // обновляем слово
            val words = Words()
            words.id = wordId
            words.word = word
            words.system = false // пользовательское слово
            words.deleted = false
            wordRepository.updateWord(words) // обновляем слово

            lettersRepository.removeLetters(wordId) // удаляем буквы
        } else { // новое слово
            val words = Words()
            words.word = word
            words.system = false // пользовательское слово
            words.deleted = false
            newWordId = wordRepository.addWord(words)!! // сохраняем слово
        }
        saveLetters(newWordId, word, missedLetters) // сохраняем буквы
    }

    /**
     * Сохраняем буквы
     */
    private fun saveLetters(wordId: Long, word: String, missedLetters: MutableList<String>) = viewModelScope.launch {
        val letters = word.uppercase().toCharArray()

        for(letterNumber in letters.indices) {
            val position = letterNumber + 1

            val oneLetter: Letters = Letters()
            oneLetter.wordId = wordId
            oneLetter.letter = letters[letterNumber].toString()
            oneLetter.position = position
            if (missedLetters[position].isNotEmpty()) { // буква, которую нужно вписать/выбрать правильное написание
                oneLetter.missed = true
                if (missedLetters[position] == context.getString(R.string.word_write_letter)) { // только павильно вписать
                    oneLetter.letterOption = ""
                    oneLetter.onlyWrite = true
                } else {
                    oneLetter.letterOption = missedLetters[position].uppercase()
                    oneLetter.onlyWrite = false
                }
            }
            val letterId: Long = lettersRepository.addLetter(oneLetter)!! // сохраняем букву
        }

        _wordAdded.value = Event(true) // close Activity
    }
}