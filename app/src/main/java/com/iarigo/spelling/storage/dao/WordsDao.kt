package com.iarigo.spelling.storage.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.iarigo.spelling.storage.entity.Words

@Dao
interface WordsDao {

    /**
     * Добавить слово
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(words: Words): Long

    /**
     * Обновить
     */
    @Update
    suspend fun update(words: Words)

    /**
     * Выбираем слово
     */
    @Query("SELECT * FROM words WHERE _id = :id")
    fun getWord(id: Long): Words

    /**
     * Выбрать все слова
     */
    @Query("SELECT * FROM words")
    fun getAll(): LiveData<List<Words>>
}