package com.iarigo.spelling.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Банк слов
 */

@Entity(indices = [Index(value = ["_id"], unique = true)],
    tableName = "words")
data class Words(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,

)
