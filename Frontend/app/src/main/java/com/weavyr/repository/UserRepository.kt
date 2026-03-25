package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.UserResponse
import com.weavyr.model.FeedResponse
import com.weavyr.model.SwipeRequest
import com.weavyr.model.SwipeResponse
import com.weavyr.model.RequestsResponse
import com.weavyr.model.MatchesResponse
// Make sure to create this data class to capture the URL your backend returns!
import com.weavyr.model.UploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

class UserRepository {

    suspend fun updateProfile(request: UpdateProfileRequest) =
        RetrofitClient.userApi.updateProfile(request)

    suspend fun fetchProfile(): Response<UserResponse> =
        RetrofitClient.userApi.fetchProfile()

    suspend fun uploadProfileImage(file: File): Response<UploadResponse> {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("profile_picture", file.name, requestFile)

        return RetrofitClient.userApi.uploadProfileImage(body)
    }

    suspend fun getUserProfileById(userId: Int): Response<UserResponse> =
        RetrofitClient.userApi.getUserProfileById(userId)

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
    suspend fun undoRejection(userId: Int): Response<Any> =
        RetrofitClient.userApi.undoRejection(userId)
}