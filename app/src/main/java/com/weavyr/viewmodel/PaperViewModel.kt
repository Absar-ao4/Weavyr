package com.weavyr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weavyr.model.Paper
import com.weavyr.repository.PaperRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaperViewModel : ViewModel() {

    private val repository = PaperRepository()

    private val _papers = MutableStateFlow<List<Paper>>(emptyList())
    val papers: StateFlow<List<Paper>> = _papers

    private var hasLoaded = false

    fun search(query: String) {

        if (hasLoaded) return
        hasLoaded = true

        viewModelScope.launch {

            delay(400)

            val result = repository.searchPapers(query)

            _papers.value = result
        }
    }
}