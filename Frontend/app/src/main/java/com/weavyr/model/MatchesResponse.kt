package com.weavyr.model

data class MatchesResponse(
    val collaborations: List<Collaboration>
)

data class Collaboration(
    val user: User
)