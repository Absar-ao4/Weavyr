package com.weavyr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weavyr.model.OpenAlexWork
import com.weavyr.repository.ArticlesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ArticlesUiState(
    val query: String = "artificial intelligence",
    val loading: Boolean = false,
    val error: String? = null,
    val openAccessOnly: Boolean = true,
    val sortByCitations: Boolean = true,
    val items: List<OpenAlexWork> = emptyList()
)

class ArticlesViewModel(
    private val repo: ArticlesRepository = ArticlesRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ArticlesUiState())
    val ui: StateFlow<ArticlesUiState> = _ui

    init {
        search()
    }

    fun setQuery(q: String) {
        _ui.value = _ui.value.copy(query = q)
    }

    fun toggleOpenAccess() {
        _ui.value = _ui.value.copy(
            openAccessOnly = !_ui.value.openAccessOnly
        )
        search()
    }

    fun toggleSort() {
        _ui.value = _ui.value.copy(
            sortByCitations = !_ui.value.sortByCitations
        )
        search()
    }

    fun search() {

        val q = _ui.value.query.trim()
        if (q.isEmpty()) return

        viewModelScope.launch {

            _ui.value = _ui.value.copy(loading = true, error = null)

            try {

                val papers = repo.searchPapers(
                    query = q,
                    openAccessOnly = _ui.value.openAccessOnly,
                    sortByCitations = _ui.value.sortByCitations
                )

                _ui.value = _ui.value.copy(
                    loading = false,
                    items = papers
                )

            } catch (e: Exception) {

                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "Error loading papers"
                )
            }
        }
    }
}