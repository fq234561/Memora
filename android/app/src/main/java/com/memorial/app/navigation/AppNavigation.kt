package com.memorial.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.memorial.app.ui.consent.ConsentScreen
import com.memorial.app.ui.create.CreateProjectScreen
import com.memorial.app.ui.download.DownloadScreen
import com.memorial.app.ui.home.HomeScreen
import com.memorial.app.ui.login.LoginScreen
import com.memorial.app.ui.preview.PreviewScreen
import com.memorial.app.ui.purchase.PurchaseScreen
import com.memorial.app.data.model.ProjectStatus
import com.memorial.app.ui.settings.SettingsScreen
import com.memorial.app.ui.style.StyleSelectionScreen
import com.memorial.app.ui.upload.UploadPhotosScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onOpenProject = { projectId, status ->
                    when (status) {
                        ProjectStatus.DRAFT ->
                            navController.navigate(Screen.UploadPhotos.createRoute(projectId))
                        ProjectStatus.UPLOADED,
                        ProjectStatus.GENERATING,
                        ProjectStatus.PREVIEW_READY,
                        ProjectStatus.FAILED ->
                            navController.navigate(Screen.Preview.createRoute(projectId))
                        ProjectStatus.PURCHASED ->
                            navController.navigate(Screen.Purchase.createRoute(projectId))
                        ProjectStatus.COMPLETED ->
                            navController.navigate(Screen.Download.createRoute(projectId))
                        else ->
                            navController.navigate(Screen.UploadPhotos.createRoute(projectId))
                    }
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.CreateProject.route) {
            CreateProjectScreen(
                onProjectCreated = { projectId ->
                    navController.navigate(Screen.UploadPhotos.createRoute(projectId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.UploadPhotos.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "memorialapp://upload_photos/{projectId}" })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            UploadPhotosScreen(
                projectId = projectId,
                onPhotosUploaded = {
                    android.util.Log.d("AppNavigation", "onPhotosUploaded called, navigating to StyleSelection for projectId=$projectId")
                    navController.navigate(Screen.StyleSelection.createRoute(projectId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StyleSelection.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "memorialapp://style_selection/{projectId}" })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            StyleSelectionScreen(
                projectId = projectId,
                onStyleSelected = {
                    navController.navigate(Screen.Consent.createRoute(projectId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Consent.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "memorialapp://consent/{projectId}" })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ConsentScreen(
                projectId = projectId,
                onConsentGiven = {
                    navController.navigate(Screen.Preview.createRoute(projectId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Preview.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "memorialapp://preview/{projectId}" })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PreviewScreen(
                projectId = projectId,
                onProceedToPurchase = {
                    navController.navigate(Screen.Purchase.createRoute(projectId))
                },
                onNavigateToDownload = {
                    navController.navigate(Screen.Download.createRoute(projectId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Purchase.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "memorialapp://purchase/{projectId}" })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PurchaseScreen(
                projectId = projectId,
                onPurchaseComplete = {
                    navController.navigate(Screen.Download.createRoute(projectId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Download.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "memorialapp://download/{projectId}" })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            DownloadScreen(
                projectId = projectId,
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRegenerate = {
                    navController.navigate(Screen.Preview.createRoute(projectId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
