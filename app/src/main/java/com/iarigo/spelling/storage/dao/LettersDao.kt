package com.iarigo.spelling.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iarigo.spelling.storage.entity.Letters
import com.iarigo.spelling.storage.entity.Words

@Dao
interface LettersDao {

    // добавлянм букву
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(letters: Letters): Long

    // удаляем все буквы слов
    @Query("DELETE FROM letters WHERE word_id = :wordId")
    suspend fun removeAll(wordId: Long)

    // выбираем буквы слова
    @Query("SELECT * FROM letters WHERE word_id = :wordId ORDER BY position ASC")
    suspend fun letters(wordId: Long): List<Letters>
}