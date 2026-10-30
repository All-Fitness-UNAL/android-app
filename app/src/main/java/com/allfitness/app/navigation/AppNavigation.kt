package com.allfitness.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.allfitness.app.R
import com.allfitness.app.auth.AuthRepository
import com.allfitness.app.auth.LoginScreen
import com.allfitness.app.auth.RegisterScreen
import com.allfitness.app.core.session.SessionManager
import com.allfitness.app.core.session.SessionState
import com.allfitness.app.core.ui.LoadingState
import com.allfitness.app.nutrition.DietWeekScreen
import com.allfitness.app.nutrition.RecipeScreen
import com.allfitness.app.onboarding.DeleteAccountScreen
import com.allfitness.app.onboarding.OnboardingScreen
import com.allfitness.app.onboarding.ProfileEditScreen
import com.allfitness.app.onboarding.ProfileScreen
import com.allfitness.app.progress.ProgressScreen
import com.allfitness.app.routines.RoutineDayScreen
import com.allfitness.app.routines.RoutineWeekScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object RoutineWeekRoute
@Serializable data class RoutineDayRoute(val date: String)
@Serializable data object DietWeekRoute
@Serializable data class RecipeRoute(val dayOfWeek: String, val mealId: String)
@Serializable data object ProgressRoute
@Serializable data object ProfileRoute
@Serializable data class ProfileSectionRoute(val section: String)
@Serializable data object DeleteAccountRoute

@HiltViewModel
class AppViewModel @Inject constructor(sessionManager: SessionManager, authRepository: AuthRepository) : ViewModel() {
    val session = sessionManager.state

    init {
        viewModelScope.launch { authRepository.restoreSession() }
    }
}

/** La sesión decide qué parte de la app se ve: Login → Onboarding → Principal (FR-005). */
@Composable
fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    when (val state = viewModel.session.collectAsStateWithLifecycle().value) {
        SessionState.Loading -> LoadingState()
        SessionState.LoggedOut -> AuthNavHost(sessionExpired = false)
        SessionState.Expired -> AuthNavHost(sessionExpired = true)
        is SessionState.Active -> if (state.session.onboardingCompleted) MainScaffold() else OnboardingScreen()
    }
}

@Composable
private fun AuthNavHost(sessionExpired: Boolean) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = LoginRoute) {
        composable<LoginRoute> { LoginScreen(sessionExpired, onRegister = { nav.navigate(RegisterRoute) }) }
        composable<RegisterRoute> { RegisterScreen(onBack = { nav.popBackStack() }) }
    }
}

private data class Tab(val route: Any, @StringRes val label: Int, @DrawableRes val icon: Int)

private val tabs = listOf(
    Tab(RoutineWeekRoute, R.string.tab_routine, R.drawable.ic_fitness),
    Tab(DietWeekRoute, R.string.tab_diet, R.drawable.ic_restaurant),
    Tab(ProgressRoute, R.string.tab_progress, R.drawable.ic_chart),
    Tab(ProfileRoute, R.string.tab_profile, R.drawable.ic_person),
)

@Composable
private fun MainScaffold() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    val back: () -> Unit = { nav.popBackStack() }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (tabs.any { destination?.hasRoute(it.route::class) == true }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = RoutineWeekRoute, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable<RoutineWeekRoute> { RoutineWeekScreen(onOpenDay = { nav.navigate(RoutineDayRoute(it.toString())) }) }
            composable<RoutineDayRoute> { RoutineDayScreen(onBack = back) }
            composable<DietWeekRoute> { DietWeekScreen(onOpenMeal = { day, mealId -> nav.navigate(RecipeRoute(day.name, mealId)) }) }
            composable<RecipeRoute> { RecipeScreen(onBack = back) }
            composable<ProgressRoute> { ProgressScreen() }
            composable<ProfileRoute> {
                ProfileScreen(
                    onEdit = { nav.navigate(ProfileSectionRoute(it.name)) },
                    onDeleteAccount = { nav.navigate(DeleteAccountRoute) },
                )
            }
            composable<ProfileSectionRoute> { ProfileEditScreen(onDone = back) }
            composable<DeleteAccountRoute> { DeleteAccountScreen(onBack = back) }
        }
    }
}
