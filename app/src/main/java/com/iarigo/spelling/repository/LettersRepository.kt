package com.iarigo.spelling.repository

import android.app.Application
import com.iarigo.spelling.storage.dao.LettersDao
import com.iarigo.spelling.storage.database.AppDatabase
import com.iarigo.spelling.storage.entity.Letters
import com.iarigo.spelling.storage.entity.Words
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class LettersRepository(application: Application): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val lettersDao: LettersDao?

    init {
        val db = AppDatabase.getAppDataBase(application)
        lettersDao = db?.lettersDao()
    }

    // добавляем букву
    suspend fun addLetter(letters: Letters) = withContext(Dispatchers.IO) {
        lettersDao?.insert(letters)
    }

    // удаляем все буквы слова
    suspend fun removeLetters(wordId: Long) = withContext(Dispatchers.IO) {
        lettersDao?.removeAll(wordId)
    }

    // выбираем буквы слова
    suspend fun getLetters(wordId: Long) = withContext(Dispatchers.IO) {
        lettersDao?.letters(wordId)
    }
}