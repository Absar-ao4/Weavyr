package com.weavyr.api

import com.weavyr.model.BookmarkResponse
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.User
import com.weavyr.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {

    @GET("users/fetchprofile")
    suspend fun fetchProfile(): Response<UserResponse>

    @PUT("users/updateprofile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @GET("users/discover")
    suspend fun getDiscoverProfiles(): Response<List<User>>

    // --- BOOKMARK ROUTES ---
    @GET("bookmarks")
    suspend fun getBookmarks(): Response<BookmarkResponse>

    @POST("bookmarks/profiles/{userId}")
    suspend fun addBookmark(@Path("userId") userId: Int): Response<Any>

    @DELETE("bookmarks/profiles/{userId}")
    suspend fun removeBookmark(@Path("userId") userId: Int): Response<Any>
}