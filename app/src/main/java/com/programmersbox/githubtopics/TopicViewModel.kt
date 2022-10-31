package com.programmersbox.githubtopics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class TopicViewModel : ViewModel() {
    private val repo = Network()

    val items = mutableStateListOf<GitHubTopic>()

    private var page = 1

    var isLoading by mutableStateOf(true)

    private val timePrinter = PrettyTime()

    init {
        viewModelScope.launch {
            loadTopics()
        }
    }

    private suspend fun loadTopics() {
        isLoading = true
        withContext(Dispatchers.IO) {
            repo.getTopics(page, "jetpack-compose").fold(
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

    // take timestamp and return a formatted string
    fun formatTimestamp(timestamp: String): String {
        val format = SimpleDateFormat.getDateTimeInstance()
        val date = Instant.parse(timestamp).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return timePrinter.format(Date(date)) + " on\n" + format.format(date)
    }
}