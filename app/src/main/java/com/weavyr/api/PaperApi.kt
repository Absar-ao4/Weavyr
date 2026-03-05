package com.weavyr.api

import com.weavyr.model.PaperResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PaperApi {

    @GET("graph/v1/paper/search")
    suspend fun searchPapers(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String =
            "title,abstract,authors,citationCount,openAccessPdf"
    ): PaperResponse
}