package com.weavyr.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weavyr.model.PaperUiModel

class BookmarksStore(context: Context) {

    private val prefs = context.getSharedPreferences("article_bookmarks", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "saved_papers"

    fun getAll(): List<PaperUiModel> {
        val json = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<PaperUiModel>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun isBookmarked(id: String): Boolean {
        return getAll().any { it.id == id }
    }

    fun toggle(paper: PaperUiModel): List<PaperUiModel> {
        val current = getAll().toMutableList()
        val index = current.indexOfFirst { it.id == paper.id }

        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(0, paper)
        }

        prefs.edit()
            .putString(key, gson.toJson(current))
            .apply()

        return current
    }
}

