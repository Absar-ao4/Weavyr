package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.LoginRequest
import com.weavyr.model.SignupRequest

class AuthRepository {

    suspend fun login(email: String, password: String) =
        RetrofitClient.api.login(
            LoginRequest(email, password)
        )

    suspend fun signup(name: String, email: String, password: String) =
        RetrofitClient.api.signup(
            SignupRequest(name, email, password)
        )
}