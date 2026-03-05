package com.weavyr.screen.main

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weavyr.viewmodel.PaperViewModel

@Composable
fun ArticlesScreen(viewModel: PaperViewModel = viewModel()) {

    val papers by viewModel.papers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.search("artificial intelligence")
    }

    if (papers.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }

    } else {

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            items(papers) { paper ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = paper.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = paper.abstract ?: "No abstract available",
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Citations: ${paper.citationCount}")
                }
            }
        }
    }
}