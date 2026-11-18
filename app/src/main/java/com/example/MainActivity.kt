package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SomTrevelDatabase
import com.example.data.SomTrevelRepository
import com.example.data.UserPreferencesRepository
import com.example.ui.components.SomTrevelTopBar
import com.example.ui.screens.*
import com.example.ui.theme.L10n
import com.example.ui.theme.SomTrevelTheme
import com.example.ui.viewmodel.SomTrevelViewModel

enum class MainNavDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", Icons.Filled.Home, Icons.Outlined.Home),
    SERVICES("services", Icons.Filled.Explore, Icons.Outlined.Explore),
    TICKETS("tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
    PORTAL("portal", Icons.Filled.Person, Icons.Outlined.Person),
    ADMIN("admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current.applicationContext
            val repository = remember {
                val db = SomTrevelDatabase.getInstance(context)
                SomTrevelRepository(context, db.dao(), UserPreferencesRepository(context))
            }
            val vm: SomTrevelViewModel = viewModel(factory = SomTrevelViewModel.Factory(repository))
            SomTrevelApp(vm)
        }
    }
}

@Composable
fun SomTrevelApp(vm: SomTrevelViewModel) {
    val prefs by vm.preferences.collectAsStateWithLifecycle()
    val language by vm.language.collectAsStateWithLifecycle()
    val snackbarMsg by vm.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.consumeSnackbar()
    }

    var currentDest by remember { mutableStateOf(MainNavDestination.HOME) }
    var serviceInitialTab by remember { mutableStateOf("FLIGHTS") }
    var serviceInitialQuery by remember { mutableStateOf("") }

    // Support system back gesture returning to Home from secondary tabs
    BackHandler(enabled = currentDest != MainNavDestination.HOME) {
        currentDest = MainNavDestination.HOME
    }

    SomTrevelTheme(darkTheme = prefs.darkMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 700.dp

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    SomTrevelTopBar(
                        language = language,
                        currencyCode = prefs.currencyCode,
                        darkMode = prefs.darkMode,
                        onToggleLanguage = { vm.toggleLanguage() },
                        onSelectCurrency = { vm.setCurrency(it) },
                        onToggleDarkMode = { vm.toggleDarkMode() }
                    )
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            MainNavDestination.entries.forEach { dest ->
                                val selected = currentDest == dest
                                val label = when (dest) {
                                    MainNavDestination.HOME -> L10n.navHome(language)
                                    MainNavDestination.SERVICES -> L10n.navExplore(language)
                                    MainNavDestination.TICKETS -> L10n.navTickets(language)
                                    MainNavDestination.PORTAL -> L10n.navPortal(language)
                                    MainNavDestination.ADMIN -> L10n.navAdmin(language)
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentDest = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            Spacer(Modifier.height(12.dp))
                            MainNavDestination.entries.forEach { dest ->
                                val selected = currentDest == dest
                                val label = when (dest) {
                                    MainNavDestination.HOME -> L10n.navHome(language)
                                    MainNavDestination.SERVICES -> L10n.navExplore(language)
                                    MainNavDestination.TICKETS -> L10n.navTickets(language)
                                    MainNavDestination.PORTAL -> L10n.navPortal(language)
                                    MainNavDestination.ADMIN -> L10n.navAdmin(language)
                                }
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentDest = dest },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("rail_${dest.route}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (currentDest) {
                            MainNavDestination.HOME -> DiscoverHomeScreen(
                                vm = vm,
                                onNavigateToService = { tab, query ->
                                    serviceInitialTab = tab
                                    serviceInitialQuery = query
                                    currentDest = MainNavDestination.SERVICES
                                },
                                onNavigateToTickets = { currentDest = MainNavDestination.TICKETS },
                                onNavigateToPortal = { currentDest = MainNavDestination.PORTAL }
                            )
                            MainNavDestination.SERVICES -> TravelServicesHubScreen(
                                vm = vm,
                                initialTab = serviceInitialTab,
                                initialQuery = serviceInitialQuery
                            )
                            MainNavDestination.TICKETS -> TicketCenterScreen(
                                vm = vm,
                                onGoToPortal = { currentDest = MainNavDestination.PORTAL }
                            )
                            MainNavDestination.PORTAL -> CustomerPortalScreen(
                                vm = vm,
                                onOpenAdminConsole = { currentDest = MainNavDestination.ADMIN }
                            )
                            MainNavDestination.ADMIN -> AdminDashboardScreen(
                                vm = vm,
                                onGoToPortal = { currentDest = MainNavDestination.PORTAL }
                            )
                        }
                    }
                }
            }
        }
    }
}
