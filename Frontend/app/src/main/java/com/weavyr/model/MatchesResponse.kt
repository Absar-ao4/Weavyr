package com.weavyr.model

data class MatchesResponse(
    val collaborations: List<CollaborationItem>
)

data class CollaborationItem(
    val id: Int,           // The ID of the connection/collaboration
    val user: User,        // THIS is the actual nested profile!
    val createdAt: String
)