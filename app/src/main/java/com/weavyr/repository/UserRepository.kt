package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.UserResponse
import com.weavyr.model.User
import retrofit2.Response

class UserRepository {

    suspend fun updateProfile(request: UpdateProfileRequest) =
        RetrofitClient.userApi.updateProfile(request)

    suspend fun fetchProfile(): Response<UserResponse> =
        RetrofitClient.userApi.fetchProfile()

    suspend fun fetchDiscoverUsers(): Response<List<User>> =
        RetrofitClient.userApi.getDiscoverProfiles()

    // --- BOOKMARK CALLS ---
    suspend fun fetchBookmarks() =
        RetrofitClient.userApi.getBookmarks()

    suspend fun addBookmark(userId: Int) =
        RetrofitClient.userApi.addBookmark(userId)

    suspend fun removeBookmark(userId: Int) =
        RetrofitClient.userApi.removeBookmark(userId)
}