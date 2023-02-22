package com.iarigo.spelling.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class PreferencesRepository(application: Application) {

    private var mSettings: SharedPreferences? =
        application.getSharedPreferences("spelling", Context.MODE_PRIVATE) // настройки приложения

    /**
     * Получаем фото
     */
    fun getPhoto(): String {
        return mSettings!!.getString("PHOTO_URI", "")!!
    }

    /**
     * Сохраняем фото
     */
    fun savePhoto(photo: String) {
        val e = mSettings?.edit()
        e!!.putString("PHOTO_URI", photo)
        e.apply()// сохраняем
    }
}