package com.weavyr.model

data class UpdateProfileRequest(
    val name: String?,
    val field: String?,
    val organization: String?,
    val experienceYears: Int?,
    val profilePhoto: String? = null,
    val education: String?,
    val numberOfPapers: Int?,
    val citationCount: Int?, // Matches parsed.data.citationCount in your backend
    val achievements: List<AchievementRequest>?,
    val interests: List<String>?, // Backend expects an array of strings!
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