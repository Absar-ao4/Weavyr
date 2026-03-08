package com.weavyr.model

data class UpdateProfileRequest(
    val name: String?,
    val field: String?,
    val organization: String?,
    // 1. Force these to be numbers instead of nulls!
    val experienceYears: Int = 0,
    val profilePhoto: String? = null,
    val education: String?,
    val numberOfPapers: Int = 0,
    val citationCount: Int = 0,
    // 2. Add the missing social fields!
    val linkedin: String? = null,
    val googlescholar: String? = null,
    val achievements: List<AchievementRequest>?,
    val interests: List<String>?,
    val papersAuthored: List<PaperRequest>?
)
data class AchievementRequest(
    val title: String,
    val description: String?,
    val year: Int?
)

data class PaperRequest(
    val authorOrder: Int,
    val title: String,
    val abstract: String?,
    val journal: String?,
    val publicationYear: Int?,
    val paperUrl: String
)