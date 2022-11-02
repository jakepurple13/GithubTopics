package com.programmersbox.githubtopics

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Serializable
data class GithubTopics(
    val items: List<GitHubTopic>
)

@Serializable
data class GitHubTopic(
    @SerialName("html_url")
    val htmlUrl: String,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val description: String? = null,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("pushed_at")
    val pushedAt: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("stargazers_count")
    val stars: Int,
    val watchers: Int,
    @SerialName("forks_count")
    val forks: Int = 0,
    val language: String = "No language",
    val owner: Owner,
    val license: License? = null,
    val topics: List<String> = emptyList()
)

@Serializable
data class Owner(
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Serializable
data class License(
    val name: String,
)

class Network {
    private val client by lazy {
        HttpClient {
            install(Logging)
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        prettyPrint = true
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                )
            }
        }
    }

    private val timePrinter = PrettyTime()
    private val format = SimpleDateFormat.getDateTimeInstance()

    suspend fun getTopics(page: Int, vararg topics: String) = runCatching {
        val url =
            "https://api.github.com/search/repositories?q=" + topics.joinToString(separator = "+") { "topic:$it" } + "+sort:updated-desc&page=$page"

        client.get(url).body<GithubTopics>().items.map {
            val date = Instant.parse(it.pushedAt).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            it.copy(pushedAt = "Updated " + timePrinter.format(Date(date)) + " on\n" + format.format(date))
        }
    }
}