package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.LoginRequest
import com.weavyr.model.SignupRequest

class AuthRepository {

    suspend fun login(email: String, password: String, rememberMe: Boolean) = // <-- Added rememberMe parameter
        RetrofitClient.authApi.login(
            LoginRequest(email, password, rememberMe) // <-- Passed to request body
        )

    suspend fun signup(username: String, email: String, password: String) =
        RetrofitClient.authApi.signup(
            SignupRequest(username, email, password)
        )
}