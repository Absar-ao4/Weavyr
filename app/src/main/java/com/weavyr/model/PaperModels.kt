package com.weavyr.model

data class PaperResponse(
    val total: Int,
    val offset: Int,
    val next: Int?,
    val data: List<Paper>
)

data class Paper(
    val paperId: String,
    val title: String,
    val abstract: String?,
    val citationCount: Int,
    val authors: List<Author>,
    val openAccessPdf: Pdf?
)

data class Author(
    val name: String
)

data class Pdf(
    val url: String?
)