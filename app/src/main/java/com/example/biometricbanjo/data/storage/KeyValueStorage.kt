package com.example.biometricbanjo.data.storage

import android.annotation.SuppressLint
import android.content.SharedPreferences
import javax.inject.Inject

@SuppressLint("ApplySharedPref")
class KeyValueStorage @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun getValue(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun storeValue(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .commit()
    }

    fun removeValue(key: String){
        sharedPreferences.edit()
            .remove(key)
            .commit()
    }

    fun clear(){
        sharedPreferences
            .edit()
            .clear()
            .commit()
    }

    fun contains(key: String): Boolean = sharedPreferences.contains(key)
}
