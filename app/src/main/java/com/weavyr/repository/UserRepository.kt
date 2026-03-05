package com.weavyr.repository

import com.weavyr.api.RetrofitClient
import com.weavyr.model.UpdateProfileRequest

class UserRepository {

    suspend fun updateProfile(request: UpdateProfileRequest) =
        RetrofitClient.userApi.updateProfile(request)

}