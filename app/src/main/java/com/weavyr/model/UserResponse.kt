package com.weavyr.model

data class UserResponse(
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val name: String?,
    val education: String?,
    val field: String?,
    val organization: String?,
    val experienceYears: Int?,
    val profilePhoto: String?,
    val numberOfPapers: Int?,
    val totalCitations: Int?,
    val achievements: List<Achievement>?,
    val interests: List<Interest>?,
    val papersAuthored: List<AuthoredPaper>? // Changed from Paper to AuthoredPaper
)

data class Achievement(
    val id: Int,
    val title: String,
    val description: String?,
    val year: Int?
)

data class Interest(
    val id: Int,
    val name: String
)

// Renamed from Paper to AuthoredPaper to fix the conflict!
data class AuthoredPaper(
    val id: Int,
    val title: String,
    val abstract: String?,
    val journal: String?,
    val publicationYear: Int?,
    val paperUrl: String?,
    val citationCount: Int?,
    val authorOrder: Int?
)