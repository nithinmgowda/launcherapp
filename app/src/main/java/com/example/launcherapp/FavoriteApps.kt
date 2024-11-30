package com.example.launcherapp

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FavoriteApps {
    private const val PREFS_NAME = "launcher_prefs"
    private const val KEY_FAVORITE_APPS = "favorite_apps"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getFavoriteApps(context: Context): List<String> {
        val json = getPrefs(context).getString(KEY_FAVORITE_APPS, "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun addFavoriteApp(context: Context, packageName: String) {
        val favorites = getFavoriteApps(context).toMutableList()
        if (!favorites.contains(packageName)) {
            favorites.add(packageName)
            saveFavoriteApps(context, favorites)
        }
    }

    fun removeFavoriteApp(context: Context, packageName: String) {
        val favorites = getFavoriteApps(context).toMutableList()
        favorites.remove(packageName)
        saveFavoriteApps(context, favorites)
    }

    private fun saveFavoriteApps(context: Context, favorites: List<String>) {
        val json = Gson().toJson(favorites)
        getPrefs(context).edit().putString(KEY_FAVORITE_APPS, json).apply()
    }
} 