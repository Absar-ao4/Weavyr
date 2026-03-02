package com.weavyr.screen.main

data class Researcher(
    val name: String,
    val organization: String,
    val location: String,
    val field: String,
    val role: String,

    val interests: List<String>,

    val papers: Int,
    val citations: Int,
    val collaborations: Int,

    val experienceYears: Int,
    val achievements: List<String>,

    val linkedIn: String? = null,
    val scholar: String? = null
)