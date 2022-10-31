package com.programmersbox.githubtopics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopicViewModel : ViewModel() {

    companion object {
        val TOPICS = arrayOf("jetpack-compose")
    }

    private val repo = Network()
    val items = mutableStateListOf<GitHubTopic>()
    var isLoading by mutableStateOf(true)
    private var page = 1

    init {
        viewModelScope.launch {
            loadTopics()
        }
    }

    private suspend fun loadTopics() {
        isLoading = true
        withContext(Dispatchers.IO) {
            repo.getTopics(page, *TOPICS).fold(
                onSuccess = { items.addAll(it) },
                onFailure = { it.printStackTrace() }
            )
        }
        isLoading = false
    }

    fun refresh() {
        viewModelScope.launch {
            items.clear()
            page = 1
            loadTopics()
        }
    }

    fun newPage() {
        page++
        viewModelScope.launch {
            loadTopics()
        }
    }
}