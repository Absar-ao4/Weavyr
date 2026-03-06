package com.weavyr.screen.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weavyr.model.OpenAlexWork
import com.weavyr.utils.openInChromeTab
import com.weavyr.viewmodel.ArticlesViewModel

private val Bg = Color(0xFF050913)
private val SurfaceDark = Color(0xCC0D1522)
private val CardDark = Color(0xCC101A29)
private val BorderDark = Color(0xFF223248)
private val AccentBlue = Color(0xFF57B8FF)
private val AccentBlueSoft = Color(0xFF183A63)
private val TextPrimary = Color(0xFFF4F7FB)
private val TextSecondary = Color(0xFF9AA8BA)

@Composable
fun ArticlesScreen(vm: ArticlesViewModel = viewModel()) {
    val state by vm.ui.collectAsState()
    val ctx = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Bg
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF040811),
                            Color(0xFF07101B),
                            Color(0xFF040811)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Research Articles",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = AccentBlue.copy(alpha = 0.22f),
                            spotColor = AccentBlue.copy(alpha = 0.22f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xCC0F1826),
                                    Color(0xB30B1220)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    AccentBlue.copy(alpha = 0.45f),
                                    Color.White.copy(alpha = 0.10f),
                                    AccentBlue.copy(alpha = 0.25f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { vm.setQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = AccentBlue.copy(alpha = 0.9f)
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Search papers...",
                                color = TextSecondary.copy(alpha = 0.8f)
                            )
                        },
                        textStyle = TextStyle(color = TextPrimary),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = AccentBlue,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLeadingIconColor = AccentBlue,
                            unfocusedLeadingIconColor = AccentBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryChip("AI")
                    CategoryChip("NLP")
                    CategoryChip("CV")
                    CategoryChip("Data")
                    CategoryChip("ML")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeavyrFilterChipDark(
                        text = "Open Access",
                        selected = state.openAccessOnly,
                        onClick = { vm.toggleOpenAccess() }
                    )

                    WeavyrFilterChipDark(
                        text = "Most Cited",
                        selected = state.sortByCitations,
                        onClick = { vm.toggleSort() }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { vm.search() },
                        enabled = !state.loading,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2D4FD7),
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(text = if (state.loading) "Loading..." else "Search")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                state.error?.let { err ->
                    Text(
                        text = err,
                        color = Color(0xFFFF6B6B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (state.loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentBlue,
                        trackColor = SurfaceDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.items) { work ->
                        PaperCardWeavyr(
                            work = work,
                            onOpen = { url -> openInChromeTab(ctx, url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String) {
    Surface(
        modifier = Modifier.shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(22.dp),
            ambientColor = AccentBlue.copy(alpha = 0.12f),
            spotColor = AccentBlue.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xCC111B2B),
                            Color(0xAA0B1420)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            AccentBlue.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.08f),
                            AccentBlue.copy(alpha = 0.18f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = text,
                color = AccentBlue,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun WeavyrFilterChipDark(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = CardDark,
            labelColor = TextSecondary,
            selectedContainerColor = AccentBlueSoft,
            selectedLabelColor = TextPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = BorderDark,
            selectedBorderColor = AccentBlue.copy(alpha = 0.55f),
            borderWidth = 1.dp
        )
    )
}

@Composable
private fun PaperCardWeavyr(
    work: OpenAlexWork,
    onOpen: (String) -> Unit
) {
    val title = work.displayName ?: "Untitled"
    val year = work.publicationYear?.toString() ?: "—"
    val cites = work.citedByCount ?: 0

    val names = work.authorships.mapNotNull { it.author?.displayName }.take(3)
    val authors = when {
        names.isEmpty() -> "Unknown authors"
        work.authorships.size > 3 -> names.joinToString(", ") + " et al."
        else -> names.joinToString(", ")
    }

    val pdf = work.primaryLocation?.pdfUrl
    val landing = work.primaryLocation?.landingPageUrl
    val openUrl = pdf ?: landing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AccentBlue.copy(alpha = 0.10f),
                spotColor = AccentBlue.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xCC101A29),
                        Color(0xB30A1220)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        AccentBlue.copy(alpha = 0.30f),
                        Color.White.copy(alpha = 0.07f),
                        AccentBlue.copy(alpha = 0.16f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = openUrl != null) {
                openUrl?.let(onOpen)
            }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = authors,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniChip("Year $year")
                MiniChip("$cites citations")

                Spacer(modifier = Modifier.weight(1f))

                FilledTonalButton(
                    onClick = { openUrl?.let(onOpen) },
                    enabled = openUrl != null,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AccentBlueSoft.copy(alpha = 0.92f),
                        contentColor = TextPrimary,
                        disabledContainerColor = SurfaceDark,
                        disabledContentColor = TextSecondary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (pdf != null) "Open PDF" else "Open")
                }
            }
        }
    }
}

@Composable
private fun MiniChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xB3152233),
                        Color(0x99101A28)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        AccentBlue.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = AccentBlue,
            style = MaterialTheme.typography.labelMedium
        )
    }
}