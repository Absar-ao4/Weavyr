package com.weavyr.model

data class PaperUiModel(
    val id: String,
    val title: String,
    val authors: String,
    val year: Int?,
    val citations: Int,
    val pdfUrl: String?,
    val landingUrl: String?
) {
    val openUrl: String?
        get() = pdfUrl ?: landingUrl
}