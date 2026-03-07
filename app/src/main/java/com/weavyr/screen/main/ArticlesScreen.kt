package com.weavyr.screen.main

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.weavyr.model.PaperUiModel
import com.weavyr.repository.ArticleCategory
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
    val selectedPaper = remember { mutableStateOf<PaperUiModel?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Bg
    ) {
        if (selectedPaper.value != null) {
            ArticleDetailScreen(
                paper = selectedPaper.value!!,
                isBookmarked = vm.isBookmarked(selectedPaper.value!!.id),
                onBack = { selectedPaper.value = null },
                onToggleBookmark = { vm.toggleBookmark(it) },
                onOpen = { url -> openInChromeTab(ctx, url) }
            )
        } else {
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
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ArticleCategory.entries.forEach { category ->
                        CategoryChip(
                            text = category.label,
                            selected = state.selectedCategory == category,
                            onClick = { vm.setCategory(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterToggleChip(
                        text = "Open Access",
                        selected = state.openAccessOnly,
                        onClick = { vm.toggleOpenAccess() }
                    )

                    FilterToggleChip(
                        text = "Most Cited",
                        selected = state.sortByCitations,
                        onClick = { vm.toggleSort() }
                    )

                    FilterToggleChip(
                        text = "Saved",
                        selected = state.savedOnly,
                        onClick = { vm.toggleSavedOnly() }
                    )

                    Button(
                        onClick = { vm.search() },
                        enabled = !state.loading,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2D4FD7),
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(if (state.loading) "Loading..." else "Search")
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
                    items(state.items) { paper ->
                        PaperCard(
                            paper = paper,
                            isBookmarked = vm.isBookmarked(paper.id),
                            onOpen = { url -> openInChromeTab(ctx, url) },
                            onToggleBookmark = { vm.toggleBookmark(it) },
                            onClick = { selectedPaper.value = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = if (selected) AccentBlueSoft else SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) AccentBlue.copy(alpha = 0.5f) else BorderDark
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            color = if (selected) TextPrimary else AccentBlue,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun FilterToggleChip(
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
private fun PaperCard(
    paper: PaperUiModel,
    isBookmarked: Boolean,
    onOpen: (String) -> Unit,
    onToggleBookmark: (PaperUiModel) -> Unit,
    onClick: (PaperUiModel) -> Unit
) {
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
            .clickable { onClick(paper) }
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = paper.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = paper.authors,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { onToggleBookmark(paper) }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isBookmarked) AccentBlue else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniChip("Year ${paper.year ?: "—"}")
                MiniChip("${paper.citations} citations")

                Spacer(modifier = Modifier.weight(1f))

                FilledTonalButton(
                    onClick = { paper.openUrl?.let(onOpen) },
                    enabled = paper.openUrl != null,
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
                    Text(if (paper.pdfUrl != null) "Open PDF" else "Open")
                }
            }
        }
    }
}

@Composable
private fun ArticleDetailScreen(
    paper: PaperUiModel,
    isBookmarked: Boolean,
    onBack: () -> Unit,
    onToggleBookmark: (PaperUiModel) -> Unit,
    onOpen: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Article Detail",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { onToggleBookmark(paper) }) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isBookmarked) AccentBlue else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = paper.title,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = paper.authors,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniChip("Year ${paper.year ?: "—"}")
            MiniChip("${paper.citations} citations")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = { onToggleBookmark(paper) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AccentBlueSoft,
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isBookmarked) "Saved" else "Save")
            }

            FilledTonalButton(
                onClick = { paper.openUrl?.let(onOpen) },
                enabled = paper.openUrl != null,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AccentBlueSoft,
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (paper.pdfUrl != null) "Read PDF" else "Open Source")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "About this paper",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = buildString {
                append("This paper was published")
                paper.year?.let { append(" in $it") }
                append(" and has ")
                append(paper.citations)
                append(" citations. ")
                append("It is authored by ${paper.authors}.")
            },
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Access",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when {
                paper.pdfUrl != null -> "A PDF version is available for direct reading."
                paper.landingUrl != null -> "PDF is not directly available, but you can open the source page."
                else -> "No reading link is available for this paper right now."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Why save this",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when {
                paper.citations >= 10000 -> "This looks like a highly influential paper and can be useful as a foundational reference."
                paper.citations >= 1000 -> "This paper is well cited and may be useful for understanding important ideas in this topic."
                else -> "This paper may be a good addition to your reading list if it matches your current research interest."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))
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