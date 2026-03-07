package com.weavyr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.weavyr.model.PaperUiModel
import com.weavyr.repository.ArticleCategory
import com.weavyr.repository.ArticlesRepository
import com.weavyr.utils.BookmarksStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ArticlesUiState(
    val query: String = "artificial intelligence",
    val loading: Boolean = false,
    val error: String? = null,
    val openAccessOnly: Boolean = true,
    val sortByCitations: Boolean = true,
    val selectedCategory: ArticleCategory = ArticleCategory.ALL,
    val savedOnly: Boolean = false,
    val items: List<PaperUiModel> = emptyList(),
    val savedItems: List<PaperUiModel> = emptyList()
)

class ArticlesViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val repo = ArticlesRepository()
    private val bookmarks = BookmarksStore(app.applicationContext)

    private val _ui = MutableStateFlow(
        ArticlesUiState(savedItems = bookmarks.getAll())
    )
    val ui: StateFlow<ArticlesUiState> = _ui

    init {
        search()
    }

    fun setQuery(q: String) {
        _ui.value = _ui.value.copy(query = q)
    }

    fun setCategory(category: ArticleCategory) {
        _ui.value = _ui.value.copy(selectedCategory = category)
        search()
    }

    fun toggleOpenAccess() {
        _ui.value = _ui.value.copy(openAccessOnly = !_ui.value.openAccessOnly)
        search()
    }

    fun toggleSort() {
        _ui.value = _ui.value.copy(sortByCitations = !_ui.value.sortByCitations)
        search()
    }

    fun toggleSavedOnly() {
        _ui.value = _ui.value.copy(savedOnly = !_ui.value.savedOnly)
        search()
    }

    fun isBookmarked(id: String): Boolean {
        return _ui.value.savedItems.any { it.id == id }
    }

    fun toggleBookmark(paper: PaperUiModel) {
        val updated = bookmarks.toggle(paper)
        _ui.value = _ui.value.copy(savedItems = updated)

        if (_ui.value.savedOnly) {
            search()
        }
    }

    fun search() {
        val state = _ui.value

        if (state.savedOnly) {
            val filtered = filterSaved(
                saved = state.savedItems,
                query = state.query,
                category = state.selectedCategory
            )
            _ui.value = state.copy(items = filtered, loading = false, error = null)
            return
        }

        viewModelScope.launch {
            _ui.value = state.copy(loading = true, error = null)

            runCatching {
                repo.searchPapers(
                    query = state.query,
                    category = state.selectedCategory,
                    openAccessOnly = state.openAccessOnly,
                    sortByCitations = state.sortByCitations
                )
            }.onSuccess { list ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    items = list,
                    error = null
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    items = emptyList(),
                    error = e.message ?: "Failed to load papers"
                )
            }
        }
    }

    private fun filterSaved(
        saved: List<PaperUiModel>,
        query: String,
        category: ArticleCategory
    ): List<PaperUiModel> {
        val q = query.trim().lowercase()
        val categoryHint = category.hint.lowercase()

        return saved.filter { paper ->
            val text = "${paper.title} ${paper.authors}".lowercase()
            val matchesQuery = q.isBlank() || text.contains(q)
            val matchesCategory = category == ArticleCategory.ALL || text.contains(categoryHint)
            matchesQuery && matchesCategory
        }
    }
}