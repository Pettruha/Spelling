package com.iarigo.spelling.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Банк слов
 * Слова деляться на пользовательские и словарные.
 * Словарные слова созданы нами. Их нельзя редактировать
 *
 * Пользовательские слова можно удалить/редактировать (? не знаю нужно ли их редактировать)
 */

@Entity(indices = [Index(value = ["_id"], unique = true)],
    tableName = "words")
data class Words(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,

    @ColumnInfo(name = "word")
    var word: String = "", // слово

    @ColumnInfo(name = "system")
    var system: Boolean = true, // Слово из словаря. Нельзя редактировать. Пользовательское можно редактировать

    @ColumnInfo(name = "deleted")
    var deleted: Boolean = false
)
