package com.pollyannawu.justwoo.android.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pollyannawu.justwoo.android.session.SessionState
import com.pollyannawu.justwoo.android.ui.auth.RegisterScreen
import com.pollyannawu.justwoo.android.ui.auth.SignInScreen
import com.pollyannawu.justwoo.android.ui.calendar.CalendarScreen
import com.pollyannawu.justwoo.android.ui.home.HomeScreen
import com.pollyannawu.justwoo.android.ui.profile.ProfileEditScreen
import com.pollyannawu.justwoo.android.ui.task.CreateTaskScreen
import org.koin.compose.koinInject

/**
 * Top-level navigation for the Android app. Mirrors the Figma flow:
 *
 *   SignIn / Register  →  Home  →  CreateTask
 *                                →  Calendar
 *                                →  ProfileEdit
 *
 * We intentionally don't navigate to a TaskDetail screen yet — that Figma
 * variant wasn't implemented in this pass, so list taps are a no-op for now.
 */
object JustWooRoutes {
    const val SIGN_IN = "signIn"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CREATE_TASK = "createTask"
    const val CALENDAR = "calendar"
    const val PROFILE_EDIT = "profileEdit"
}

@Composable
fun JustWooNavHost() {
    val navController = rememberNavController()
    val session: SessionState = koinInject()
    val user by session.user.collectAsState()
    val houseId by session.houseId.collectAsState()

    NavHost(navController = navController, startDestination = JustWooRoutes.SIGN_IN) {

        composable(JustWooRoutes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(JustWooRoutes.HOME) {
                        popUpTo(JustWooRoutes.SIGN_IN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(JustWooRoutes.REGISTER) },
            )
        }

        composable(JustWooRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(JustWooRoutes.HOME) {
                        popUpTo(JustWooRoutes.SIGN_IN) { inclusive = true }
                    }
                },
                onNavigateToSignIn = { navController.popBackStack() },
            )
        }

        composable(JustWooRoutes.HOME) {
            HomeScreen(
                currentUserId = user?.id ?: 0L,
                currentHouseId = houseId ?: 0L,
                onCreateTask = { navController.navigate(JustWooRoutes.CREATE_TASK) },
                onOpenTask = { /* TODO: task detail route */ },
                onOpenCalendar = { navController.navigate(JustWooRoutes.CALENDAR) },
                onOpenProfile = { navController.navigate(JustWooRoutes.PROFILE_EDIT) },
            )
        }

        composable(JustWooRoutes.CREATE_TASK) {
            CreateTaskScreen(
                currentUserId = user?.id ?: 0L,
                currentHouseId = houseId ?: 0L,
                onClose = { navController.popBackStack() },
            )
        }

        composable(JustWooRoutes.CALENDAR) {
            CalendarScreen(
                onClose = { navController.popBackStack() },
                onOpenTask = { /* TODO: task detail route */ },
            )
        }

        composable(JustWooRoutes.PROFILE_EDIT) {
            ProfileEditScreen(
                onClose = { navController.popBackStack() },
            )
        }
    }
}
