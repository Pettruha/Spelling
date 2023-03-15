package com.iarigo.spelling.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Буквы из которых состоят слова
 * Слово разбивается на буквы
 * Букву, которую нужно подставить, помечается и создаются варианты написания с пометкой правильно/неправильно
 */

@Entity(indices = [Index(value = ["_id"], unique = true)],
tableName = "letters")
data class Letters(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,

    @ColumnInfo(name = "word_id")
    var wordId: Long = 0L, // слово

    @ColumnInfo(name = "letter")
    var letter: String = "", // буква слова

    @ColumnInfo(name = "missed")
    var missed: Boolean = false, // пропущена буква при выводе или нет

    @ColumnInfo(name = "position")
    var position: Int = 0 // позиция буквы в слове
)