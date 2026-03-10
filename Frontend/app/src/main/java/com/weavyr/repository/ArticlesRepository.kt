package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.OpenAlexWork
import com.weavyr.model.PaperUiModel

enum class ArticleCategory(val label: String, val hint: String) {
    ALL("All", ""),
    AI("AI", "artificial intelligence"),
    NLP("NLP", "natural language processing"),
    CV("CV", "computer vision"),
    DATA("Data", "data science"),
    ML("ML", "machine learning")
}

class ArticlesRepository {

    companion object {
        // Put your free OpenAlex API key here
        private const val OPENALEX_API_KEY = "https://api.openalex.org"
    }

    suspend fun searchPapers(
        query: String,
        category: ArticleCategory,
        openAccessOnly: Boolean,
        sortByCitations: Boolean
    ): List<PaperUiModel> {

        val fullQuery = buildString {
            append(query.trim())
            if (category != ArticleCategory.ALL) {
                if (isNotBlank()) append(" ")
                append(category.hint)
            }
        }.trim()

        val filter = if (openAccessOnly) "is_oa:true" else null
        val sort = if (sortByCitations) "cited_by_count:desc" else null

        return RetrofitClient.openAlexApi.searchWorks(
            query = if (fullQuery.isBlank()) "research" else fullQuery,
            perPage = 20,
            page = 1,
            filter = filter,
            sort = sort
        ).results.map { it.toUiModel() }
    }

    private fun OpenAlexWork.toUiModel(): PaperUiModel {
        val names = authorships.mapNotNull { it.author?.displayName }.take(3)
        val authorText = when {
            names.isEmpty() -> "Unknown authors"
            authorships.size > 3 -> names.joinToString(", ") + " et al."
            else -> names.joinToString(", ")
        }

        return PaperUiModel(
            id = id ?: displayName ?: System.currentTimeMillis().toString(),
            title = displayName ?: "Untitled",
            authors = authorText,
            year = publicationYear,
            citations = citedByCount ?: 0,
            pdfUrl = primaryLocation?.pdfUrl,
            landingUrl = primaryLocation?.landingPageUrl
        )
    }
}