package com.weavyr.repository

import android.util.Log
import com.weavyr.api.RetrofitClient
import com.weavyr.model.Paper

class PaperRepository {

    suspend fun searchPapers(query: String): List<Paper> {

        return try {

            val response = RetrofitClient.paperApi.searchPapers(query)

            Log.d("WEAVYR_API", "API response received: ${response.data.size} papers")

            response.data

        } catch (e: Exception) {

            Log.e("WEAVYR_API", "API ERROR: ${e.message}")

            emptyList()
        }
    }
}