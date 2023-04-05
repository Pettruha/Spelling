package com.iarigo.spelling.ui.words

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.iarigo.spelling.repository.LettersRepository
import com.iarigo.spelling.repository.WordRepository
import com.iarigo.spelling.storage.entity.Words

class WordsViewModel(application: Application): AndroidViewModel(application) {

    private var wordRepository: WordRepository = WordRepository(application)
    private var lettersRepository: LettersRepository = LettersRepository(application)

    // список слов. Выборка. Сортировка
    val wordList: MutableLiveData<Bundle> = MutableLiveData()

    // обновляем список слов
    val statObservable: LiveData<List<Words>> = Transformations.switchMap(wordList) { param ->
        getWords()
    }

    /**
     * Получаем список слов
     */
    private fun getWords(): LiveData<List<Words>> {
        return wordRepository.getWords()!!
    }
}