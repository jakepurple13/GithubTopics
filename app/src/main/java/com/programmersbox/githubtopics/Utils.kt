package com.programmersbox.githubtopics

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.serialization.json.Json

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("LocalNavController")
}

val LocalJson = staticCompositionLocalOf {
    Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}

enum class Screen(val route: String) {
    Topics("Topics"),
    Repo("Repo")
}

fun NavController.navigate(route: Screen) = navigate(route.route)