package com.weavyr.model

data class UserResponse(
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String?, // Nullable since GET /:id (public route) might not return it
    val name: String?,
    val education: String?,
    val field: String?,
    val organization: String?,
    val experienceYears: Int?,
    val profilePhoto: String?,
    val numberOfPapers: Int?,
    val totalCitations: Int?,
    val achievements: List<Achievement>? = emptyList(),
    val interests: List<Interest>? = emptyList(),
    val papersAuthored: List<AuthoredPaper>? = emptyList(),
    val badges: List<Badge>? = emptyList() // Added to support your public profile route!
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

data class Badge(
    val id: Int,
    val name: String,
    val description: String?
)

// Renamed from Paper to AuthoredPaper to fix the conflict!
data class AuthoredPaper(
    val id: String, // Changed to String because your backend uses paperUrl as the ID: `where: { id: p.paperUrl }`
    val title: String,
    val abstract: String?,
    val journal: String?,
    val publicationYear: Int?,
    val paperUrl: String?,
    val citationCount: Int?,
    val authorOrder: Int?
)