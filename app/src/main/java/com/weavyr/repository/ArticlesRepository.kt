package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.OpenAlexWork

class ArticlesRepository {

    suspend fun searchPapers(
        query: String,
        openAccessOnly: Boolean,
        sortByCitations: Boolean
    ): List<OpenAlexWork> {
        val filter = if (openAccessOnly) "open_access.is_oa:true" else null
        val sort = if (sortByCitations) "cited_by_count:desc" else null

        return RetrofitClient.openAlexApi
            .searchWorks(
                query = query,
                perPage = 25,
                page = 1,
                filter = filter,
                sort = sort
            )
            .results
    }
}