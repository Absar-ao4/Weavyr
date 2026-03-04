package com.weavyr.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weavyr.viewmodel.MainViewModel
import com.weavyr.ui.theme.*

@Composable
fun ProfileListsScreen(
    title: String,
    profiles: List<Researcher>
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = WeavyrTextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {

            items(profiles) { profile ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WeavyrCard
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = profile.name,
                            color = WeavyrTextPrimary
                        )

                        Text(
                            text = profile.organization,
                            color = WeavyrTextSecondary
                        )

                        Text(
                            text = profile.field,
                            color = WeavyrTextSecondary
                        )
                    }
                }
            }
        }
    }
}