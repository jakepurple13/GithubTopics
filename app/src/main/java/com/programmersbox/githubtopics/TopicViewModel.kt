package com.programmersbox.githubtopics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class TopicViewModel(private val store: DataStore<TopicSettings>) : ViewModel() {

    val items = mutableStateListOf<GitHubTopic>()
    var isLoading by mutableStateOf(true)
    private var page = 1
    val currentTopics = mutableStateListOf<String>()
    val topicList = mutableStateListOf<String>()

    init {
        store.data
            .map { it.currentTopicsListList }
            .distinctUntilChanged()
            .onEach {
                currentTopics.clear()
                currentTopics.addAll(it)
            }
            .filter { it.isNotEmpty() && it.all { t -> t.isNotEmpty() } }
            .onEach { refresh() }
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
            Network.getTopics(page, *currentTopics.toTypedArray()).fold(
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
        viewModelScope.launch { loadTopics() }
    }

    fun addTopic(topic: String) {
        if (topic !in topicList && topic.isNotEmpty()) {
            viewModelScope.launch { store.update { addTopicList(topic) } }
        }
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
        viewModelScope.launch {
            store.update {
                if (topic !in currentTopicsListList) {
                    addCurrentTopicsList(topic)
                } else {
                    val list = currentTopicsListList.toMutableList()
                    list.remove(topic)
                    clearCurrentTopicsList()
                    addAllCurrentTopicsList(list)
                }
            }
        }
    }
}

class RepoViewModel(handle: SavedStateHandle) : ViewModel() {
    val item by lazy {
        handle.get<String>("topic")!!.let { Json.decodeFromString<GitHubTopic>(it) }
    }

    var repo by mutableStateOf("")
    var error by mutableStateOf(false)
    var loading by mutableStateOf(true)

    init {
        viewModelScope.launch {
            Network.getReadMe(item.fullName).fold(
                onSuccess = { repo = it },
                onFailure = {
                    it.printStackTrace()
                    error = true
                }
            )
            loading = false
        }
    }
}