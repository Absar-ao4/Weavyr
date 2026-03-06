package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.UserResponse
import retrofit2.Response

class UserRepository {

    suspend fun updateProfile(request: UpdateProfileRequest) =
        RetrofitClient.userApi.updateProfile(request)

    // ADDED THIS:
    suspend fun fetchProfile(): Response<UserResponse> =
        RetrofitClient.userApi.fetchProfile()

}