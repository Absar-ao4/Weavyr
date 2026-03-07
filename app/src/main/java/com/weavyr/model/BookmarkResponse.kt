package com.weavyr.model

data class BookmarkResponse(
    val profileBookmarks: List<User> // Reuses your existing User model!
)