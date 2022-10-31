package com.programmersbox.githubtopics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TopicViewModel(private val store: DataStore<TopicSettings>) : ViewModel() {

    private val repo = Network()
    val items = mutableStateListOf<GitHubTopic>()
    var isLoading by mutableStateOf(true)
    private var page = 1
    var currentTopic by mutableStateOf("")
    val topicList = mutableStateListOf<String>()

    init {
        store.data
            .map { it.currentTopic }
            .distinctUntilChanged()
            .onEach {
                currentTopic = it
                if (it.isNotEmpty()) {
                    refresh()
                }
            }
            .launchIn(viewModelScope)

        store.data.map { it.topicListList }
            .distinctUntilChanged()
            .onEach {
                topicList.clear()
                topicList.addAll(it)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadTopics() {
        isLoading = true
        withContext(Dispatchers.IO) {
            repo.getTopics(page, currentTopic).fold(
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

    fun addTopic(topic: String) {
        if (topic !in topicList)
            viewModelScope.launch { store.update { addTopicList(topic) } }
    }

    fun removeTopic(topic: String) {
        viewModelScope.launch {
            store.update {
                val list = topicListList.toMutableList()
                list.remove(topic)
                clearTopicList()
                addAllTopicList(list)
            }
        }
    }

    fun setTopic(topic: String) {
        viewModelScope.launch { store.update { setCurrentTopic(topic) } }
    }
}