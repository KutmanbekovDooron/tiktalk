package com.andyshon.tiktalk.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.andyshon.tiktalk.di.qualifier.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context,
    @Named("basic") gs: Gson
) {
    private val prefsName: String = "${context.packageName}_shared_prefs"
    private val preferences: SharedPreferences
    private val gson = gs

    init {
        preferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun putValue(key: String, value: PreferenceType) {
        val editor = preferences.edit()

        when (value) {
            is PreferenceType.String -> editor.putString(key, value.value)
            is PreferenceType.Int -> editor.putInt(key, value.value)
            is PreferenceType.Boolean -> editor.putBoolean(key, value.value)
        }

        editor.apply()
    }

    fun putStringSet(key: String, obj: Set<String>) {
        val editor = preferences.edit()

        editor.putStringSet(key, obj)
        editor.apply()
    }

    fun <T> putObject(key: String, obj: T, clazz: Class<T>) {
        val editor = preferences.edit()

        editor.putString(key, gson.toJson(obj, clazz))
        editor.apply()
    }

    fun <T> getObject(key: String, clazz: Class<T>): T? {
        return gson.fromJson(preferences.getString(key, null), clazz)
    }

    fun getStringSet(key: String): MutableSet<String>? {
        return preferences.getStringSet(key, null)
    }

    fun getString(key: String, fallback: String = ""): String {
        return  preferences.getString(key, fallback)!!
    }

    fun getInt(key: String, fallback: Int = 0): Int {
        return preferences.getInt(key, fallback)
    }

    fun getBoolean(key: String, fallback: Boolean = false): Boolean {
        return preferences.getBoolean(key, fallback)
    }

    fun has(key: String): Boolean {
        return preferences.contains(key)
    }

    fun removeValue(key: String) {
        preferences.edit().remove(key).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    sealed class PreferenceType {

        class Int(val value: kotlin.Int) : PreferenceType()
        class String(val value: kotlin.String) : PreferenceType()
        class Boolean(val value: kotlin.Boolean) : PreferenceType()

    }
}