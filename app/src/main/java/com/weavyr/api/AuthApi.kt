package com.weavyr.api

import com.weavyr.model.LoginRequest
import com.weavyr.model.LoginResponse
import com.weavyr.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<LoginResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

}