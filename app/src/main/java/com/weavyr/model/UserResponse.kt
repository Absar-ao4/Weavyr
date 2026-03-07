package com.weavyr.model

data class UserResponse(
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String?,
    val name: String?,
    val education: String?,
    val field: String?,
    val organization: String?,
    val experienceYears: Int?,
    val profilePhoto: String?,
    val numberOfPapers: Int?,
    val totalCitations: Int?,
    val achievements: List<Achievement>? = emptyList(),
    val interests: List<String>? = emptyList(),   // FIXED HERE
    val papersAuthored: List<AuthoredPaper>? = emptyList(),
    val badges: List<Badge>? = emptyList()
)

data class Achievement(
    val id: Int,
    val title: String,
    val description: String?,
    val year: Int?
)

data class Badge(
    val id: Int,
    val name: String,
    val description: String?
)

data class AuthoredPaper(
    val id: String,
    val title: String,
    val abstract: String?,
    val journal: String?,
    val publicationYear: Int?,
    val paperUrl: String?,
    val citationCount: Int?,
    val authorOrder: Int?
)