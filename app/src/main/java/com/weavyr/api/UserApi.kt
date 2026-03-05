package com.weavyr.api

import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {

    // Fixed the path to match your backend
    @PUT("users/updateprofile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserResponse>

    // Added the fetch profile endpoint for you
    @GET("users/fetchprofile")
    suspend fun fetchProfile(): Response<UserResponse>

}