package com.weavyr.model

data class UserResponse(
    val message: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val name: String?,
    val qualification: String?,
    val field: String?,
    val organization: String?,
    val experienceYears: Int?,
    val profilePhoto: String?,
    val achievements: List<String>?,
    val totalCitations: Int?,
    val interests: List<String>?,
    val papersAuthored: Int?
)