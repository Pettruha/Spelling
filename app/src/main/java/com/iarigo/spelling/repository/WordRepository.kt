package com.iarigo.spelling.repository

import android.app.Application
import com.iarigo.spelling.storage.dao.LettersDao
import com.iarigo.spelling.storage.dao.WordsDao
import com.iarigo.spelling.storage.database.AppDatabase
import com.iarigo.spelling.storage.entity.Words
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class WordRepository(application: Application): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val wordsDao: WordsDao?

    init {
        val db = AppDatabase.getAppDataBase(application)
        wordsDao = db?.wordsDao()
    }

    /**
     * Добавляем запись
     */
    suspend fun addWord(words: Words) = withContext(Dispatchers.IO) {
        wordsDao?.insert(words)
    }

    /**
     * Обновляем запись
     */
    suspend fun updateWord(words: Words) = withContext(Dispatchers.IO) {
        wordsDao?.update(words)
    }

    /**
     * Выбираем слово
     */
    suspend fun getWord(id: Long) = withContext(Dispatchers.IO) {
        wordsDao?.getWord(id)
    }

    /**
     * Выбираем слова
     */
    fun getWords() = wordsDao?.getAll()
}