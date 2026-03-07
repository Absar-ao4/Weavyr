package com.weavyr.model

data class Researcher(
    val id: Int, // Real backend ID for swiping
    val name: String,
    val organization: String,
    val field: String,
    val interests: List<String>,
    val papers: Int,
    val citations: Int,
    val experienceYears: Int,
    val achievements: List<String>
)