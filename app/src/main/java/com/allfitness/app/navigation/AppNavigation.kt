package com.allfitness.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
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
import com.allfitness.app.core.ui.theme.AppTheme
import com.allfitness.app.core.ui.theme.Motion
import com.allfitness.app.core.ui.theme.Spacing
import com.allfitness.app.home.HomeScreen
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
@Serializable data object HomeRoute
@Serializable data object RoutineWeekRoute
@Serializable data class RoutineDayRoute(val date: String, val index: Int = 0)
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

// Lucide no tiene variantes rellenas: el mismo ícono sirve seleccionado o no.
private data class Tab(val route: Any, @StringRes val label: Int, @DrawableRes val icon: Int)

private val tabs = listOf(
    Tab(HomeRoute, R.string.tab_home, R.drawable.lc_house),
    Tab(RoutineWeekRoute, R.string.tab_routine, R.drawable.lc_dumbbell),
    Tab(DietWeekRoute, R.string.tab_diet, R.drawable.lc_apple),
    Tab(ProgressRoute, R.string.tab_progress, R.drawable.lc_chart_line),
    Tab(ProfileRoute, R.string.tab_profile, R.drawable.lc_user),
)

/** Pantallas que se abren desde una pestaña: entran de lado. Entre pestañas, fundido. */
private val detailRoutes = listOf(RoutineDayRoute::class, RecipeRoute::class, ProfileSectionRoute::class, DeleteAccountRoute::class)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isDetailMove() =
    listOf(initialState, targetState).any { entry -> detailRoutes.any { entry.destination.hasRoute(it) } }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enter(reduced: Boolean, pop: Boolean): EnterTransition = when {
    reduced -> fadeIn(tween(Motion.SHORT))
    isDetailMove() -> slideInHorizontally(tween(Motion.MEDIUM, easing = Motion.Emphasized)) { if (pop) -it / 10 else it / 10 } +
        fadeIn(tween(Motion.MEDIUM, easing = Motion.Emphasized))
    else -> fadeIn(tween(210, delayMillis = 90)) + scaleIn(tween(210, delayMillis = 90), initialScale = 0.96f)
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exit(reduced: Boolean, pop: Boolean): ExitTransition = when {
    reduced -> fadeOut(tween(Motion.SHORT))
    isDetailMove() -> slideOutHorizontally(tween(Motion.MEDIUM, easing = Motion.Emphasized)) { if (pop) it / 10 else -it / 10 } +
        fadeOut(tween(Motion.MEDIUM, easing = Motion.Emphasized))
    else -> fadeOut(tween(90))
}

@Composable
private fun MainScaffold() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination
    val back: () -> Unit = { nav.popBackStack() }
    val scheme = MaterialTheme.colorScheme
    val reduced = AppTheme.reducedMotion
    // Sin píldora indicadora (Figma): la pestaña activa solo cambia de color.
    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            unselectedIconColor = scheme.onSurfaceVariant,
            unselectedTextColor = scheme.onSurfaceVariant,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = scheme.primary,
            selectedTextColor = scheme.primary,
            unselectedIconColor = scheme.onSurfaceVariant,
            unselectedTextColor = scheme.onSurfaceVariant,
        ),
    )
    val suiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = scheme.surface,
        navigationRailContainerColor = scheme.surface,
    )
    // Barra en el teléfono, riel en tableta; oculta en las pantallas de detalle.
    val layoutType = if (tabs.any { destination?.hasRoute(it.route::class) == true }) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    } else {
        NavigationSuiteType.None
    }
    val hairline = scheme.outlineVariant
    // Como NavigationSuiteScaffold: la barra (o el riel) ya cubre su inset, así que el Scaffold de cada pestaña no lo repite.
    val consumed = when (layoutType) {
        NavigationSuiteType.NavigationBar -> NavigationBarDefaults.windowInsets.only(WindowInsetsSides.Bottom)
        NavigationSuiteType.NavigationRail -> NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start)
        else -> WindowInsets(0, 0, 0, 0)
    }

    Surface(color = scheme.background, contentColor = scheme.onBackground) {
        NavigationSuiteScaffoldLayout(
            navigationSuite = {
                NavigationSuite(
                    // El hilo superior se dibuja sobre el contenedor de la barra (drawBehind quedaría bajo su fondo); el riel no lo lleva.
                    modifier = if (layoutType == NavigationSuiteType.NavigationBar) {
                        Modifier.drawWithContent {
                            drawContent()
                            drawRect(hairline, size = Size(size.width, Spacing.hairline.toPx()))
                        }
                    } else {
                        Modifier
                    },
                    layoutType = layoutType,
                    colors = suiteColors,
                ) {
                    tabs.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
                        item(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = {
                                // Con fuente al 200 % "Progreso" no cabe en 10 sp: se reduce lo justo (6 sp × 2 sigue siendo mayor que 10 sp × 1) en vez de cortarse.
                                val style = MaterialTheme.typography.labelSmall
                                Text(
                                    stringResource(tab.label),
                                    style = style,
                                    maxLines = 1,
                                    autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = style.fontSize),
                                )
                            },
                            colors = itemColors,
                        )
                    }
                }
            },
            layoutType = layoutType,
        ) {
            // Ancho máximo de lectura en pantallas grandes (spec 4.5); en el teléfono no cambia nada.
            Box(Modifier.fillMaxSize().consumeWindowInsets(consumed), contentAlignment = Alignment.TopCenter) {
                NavHost(
                    nav,
                    startDestination = HomeRoute,
                    modifier = Modifier.widthIn(max = Spacing.contentMaxWidth),
                    enterTransition = { enter(reduced, pop = false) },
                    exitTransition = { exit(reduced, pop = false) },
                    popEnterTransition = { enter(reduced, pop = true) },
                    popExitTransition = { exit(reduced, pop = true) },
                ) {
                    composable<HomeRoute> { HomeScreen() }
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
    }
}
