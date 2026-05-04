package com.weavyr.model

data class Researcher(
    val id: Int,
    val username: String,
    val name: String?,
    val email: String?,
    val organization: String?,
    val field: String?,
    val interests: List<String>,
    val papers: Int,
    val citations: Int,
    val experienceYears: Int,
    val achievements: List<String>,
    val profilePhoto: String?,
    val roles: List<String> = emptyList()
)