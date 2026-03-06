package com.weavyr.api

import com.weavyr.model.OpenAlexResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenAlexApi {

    @GET("works")
    suspend fun searchWorks(
        @Query("search") query: String,
        @Query("per-page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("filter") filter: String? = null,
        @Query("sort") sort: String? = null
    ): OpenAlexResponse
}