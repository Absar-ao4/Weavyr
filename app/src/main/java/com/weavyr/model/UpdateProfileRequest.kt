package com.weavyr.model

data class UpdateProfileRequest(
    val name: String,
    val education: String,
    val field: String?,
    val organization: String?,
    val experienceYears: Int?,
    val interests: List<String>,
    val numberOfPapers: Int?,
    val citationCount: Int?,
    val achievements: List<AchievementRequest>?
)

data class AchievementRequest(
    val title: String,
    val description: String? = null,
    val year: Int? = null
)