package com.iarigo.spelling.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Пропущенные буквы в словах.
 * Позиция пропущенной буквы в слове
 * Варианты написания буквы
 * Правильна буква
 */

@Entity(indices = [Index(value = ["_id"], unique = true)],
    tableName = "missed_letters")
data class MissedLetters(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,

    @ColumnInfo(name = "word_id")
    var wordId: Long = 0L, // слово

    @ColumnInfo(name = "letter_id")
    var letterId: Long = 0L, // буква слова, которую нужно правильно написать

    @ColumnInfo(name = "option")
    var letterOption: String = "", // вариант написания пропущенной буквы

    @ColumnInfo(name = "correct")
    var correct: Boolean = true // правильный вариант написания буквы или нет
)
