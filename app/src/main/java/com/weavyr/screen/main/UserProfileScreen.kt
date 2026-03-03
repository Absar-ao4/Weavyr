package com.weavyr.screen.main

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.weavyr.viewmodel.MainViewModel
import androidx.compose.material3.Text

@Composable
fun UserProfileScreen(viewModel: MainViewModel) {

    Column {
        Text("Bookmarks: ${viewModel.bookmarkedProfiles.size}")
        Text("Rejected: ${viewModel.rejectedProfiles.size}")
        Text("Connection Requests: ${viewModel.connectionRequests.size}")
    }
}