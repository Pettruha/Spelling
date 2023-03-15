package com.iarigo.spelling.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.iarigo.spelling.storage.entity.Words

@Dao
interface MissedLettersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(words: Words): Long
}