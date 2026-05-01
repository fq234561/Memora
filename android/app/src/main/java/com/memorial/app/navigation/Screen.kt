package com.memorial.app.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object CreateProject : Screen("create_project")
    data object UploadPhotos : Screen("upload_photos/{projectId}") {
        fun createRoute(projectId: String) = "upload_photos/$projectId"
    }
    data object StyleSelection : Screen("style_selection/{projectId}") {
        fun createRoute(projectId: String) = "style_selection/$projectId"
    }
    data object Consent : Screen("consent/{projectId}") {
        fun createRoute(projectId: String) = "consent/$projectId"
    }
    data object Preview : Screen("preview/{projectId}") {
        fun createRoute(projectId: String) = "preview/$projectId"
    }
    data object Purchase : Screen("purchase/{projectId}") {
        fun createRoute(projectId: String) = "purchase/$projectId"
    }
    data object Download : Screen("download/{projectId}") {
        fun createRoute(projectId: String) = "download/$projectId"
    }
    data object Settings : Screen("settings")
}
