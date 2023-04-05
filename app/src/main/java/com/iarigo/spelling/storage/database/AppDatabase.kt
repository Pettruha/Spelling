package com.iarigo.spelling.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iarigo.spelling.storage.dao.LettersDao
import com.iarigo.spelling.storage.dao.WordsDao
import com.iarigo.spelling.storage.entity.Letters
import com.iarigo.spelling.storage.entity.Words

@Database(
    entities = [Words::class, Letters::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordsDao(): WordsDao // слова
    abstract fun lettersDao(): LettersDao // буквы

    companion object {
        @Volatile
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "spelling.db")
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}