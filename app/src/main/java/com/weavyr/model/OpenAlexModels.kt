package com.weavyr.model

import com.google.gson.annotations.SerializedName

data class OpenAlexResponse(
    @SerializedName("results")
    val results: List<OpenAlexWork> = emptyList()
)

data class OpenAlexWork(

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("display_name")
    val displayName: String? = null,

    @SerializedName("publication_year")
    val publicationYear: Int? = null,

    @SerializedName("cited_by_count")
    val citedByCount: Int? = null,

    @SerializedName("primary_location")
    val primaryLocation: PrimaryLocation? = null,

    @SerializedName("authorships")
    val authorships: List<Authorship> = emptyList()
)

data class PrimaryLocation(

    @SerializedName("landing_page_url")
    val landingPageUrl: String? = null,

    @SerializedName("pdf_url")
    val pdfUrl: String? = null
)

data class Authorship(

    @SerializedName("author")
    val author: Author? = null
)

data class Author(

    @SerializedName("display_name")
    val displayName: String? = null
)