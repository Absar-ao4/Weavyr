package com.weavyr.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.weavyr.screen.main.Researcher

class MainViewModel : ViewModel() {

    // Temporary in-memory storage (acts like backend for now)

    val bookmarkedProfiles = mutableStateListOf<Researcher>()
    val rejectedProfiles = mutableStateListOf<Researcher>()
    val watchedProfiles = mutableStateListOf<Researcher>()
    val connectionRequests = mutableStateListOf<Researcher>()

    fun addBookmark(profile: Researcher) {
        if (!bookmarkedProfiles.contains(profile)) {
            bookmarkedProfiles.add(profile)
        }
    }


    fun addConnectionRequest(profile: Researcher) {
        if (!connectionRequests.contains(profile)) {
            connectionRequests.add(profile)
        }
    }

    fun addRejected(profile: Researcher) {
        if (!rejectedProfiles.contains(profile)) {
            rejectedProfiles.add(profile)
        }
    }

    fun addWatched(profile: Researcher) {
        if (!watchedProfiles.contains(profile)) {
            watchedProfiles.add(profile)
        }
    }
}