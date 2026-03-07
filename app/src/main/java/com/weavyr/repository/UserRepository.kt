package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.UserResponse
import com.weavyr.model.FeedResponse
import com.weavyr.model.SwipeRequest
import com.weavyr.model.SwipeResponse
import com.weavyr.model.RequestsResponse
import com.weavyr.model.MatchesResponse
import retrofit2.Response

class UserRepository {

    // --- Profile Management ---
    suspend fun updateProfile(request: UpdateProfileRequest) =
        RetrofitClient.userApi.updateProfile(request)

    suspend fun fetchProfile(): Response<UserResponse> =
        RetrofitClient.userApi.fetchProfile()

    // --- Discovery & Swiping ---
    suspend fun getDiscoverFeed(): Response<FeedResponse> =
        RetrofitClient.userApi.getDiscoverFeed()

    suspend fun recordSwipe(targetUserId: Int, action: String): Response<SwipeResponse> {
        val request = SwipeRequest(targetUserId, action)
        return RetrofitClient.userApi.recordSwipe(request)
    }
    suspend fun getMatches(): Response<MatchesResponse> =
        RetrofitClient.userApi.getMatches()
    suspend fun fetchBookmarks() =
        RetrofitClient.userApi.getBookmarks()

    suspend fun addBookmark(userId: Int) =
        RetrofitClient.userApi.addBookmark(userId)

    suspend fun getIncomingRequests() = RetrofitClient.userApi.getIncomingRequests()

    suspend fun getSentRequests() = RetrofitClient.userApi.getSentRequests()

    suspend fun getRejectedProfiles() = RetrofitClient.userApi.getRejectedProfiles()

    suspend fun removeBookmark(userId: Int) =
        RetrofitClient.userApi.removeBookmark(userId)
}
