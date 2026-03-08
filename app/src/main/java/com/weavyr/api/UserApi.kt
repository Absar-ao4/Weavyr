package com.weavyr.api

import com.weavyr.model.BookmarkResponse
import com.weavyr.model.FeedResponse
import com.weavyr.model.RequestsResponse
import com.weavyr.model.SwipeRequest
import com.weavyr.model.SwipeResponse
import com.weavyr.model.UpdateProfileRequest
import com.weavyr.model.User
import com.weavyr.model.UserResponse
import com.weavyr.model.MatchesResponse
import com.weavyr.model.RejectedResponse
import com.weavyr.model.SentResponse
import com.weavyr.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @GET("users/fetchprofile")
    suspend fun fetchProfile(): Response<UserResponse>

    @PUT("users/updateprofile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @GET("discover/feed")
    suspend fun getDiscoverFeed(): Response<FeedResponse>

    @POST("swipes")
    suspend fun recordSwipe(@Body request: SwipeRequest): Response<SwipeResponse>

    @GET("swipes/requests")
    suspend fun getIncomingRequests(): Response<RequestsResponse>

    // ⭐ NEW: Fetch matched collaborators
    @GET("collaborations")
    suspend fun getMatches(): Response<MatchesResponse>

    @GET("bookmarks")
    suspend fun getBookmarks(): Response<BookmarkResponse>

    @POST("bookmarks/profiles/{userId}")
    suspend fun addBookmark(@Path("userId") userId: Int): Response<Any>

    @DELETE("bookmarks/profiles/{userId}")
    suspend fun removeBookmark(@Path("userId") userId: Int): Response<Any>

    @GET("swipes/sent")
    suspend fun getSentRequests(): Response<SentResponse>

    @GET("swipes/rejected")
    suspend fun getRejectedProfiles(): Response<RejectedResponse>

    @Multipart
    @POST("user/upload-profile-picture") // Ask your backend dev for the exact route path!
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>


}
