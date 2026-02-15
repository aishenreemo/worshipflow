package com.example.workshipflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workshipflow.presentation.browser.BrowserScreen
import com.example.workshipflow.presentation.calendar.AddServiceScreen
import com.example.workshipflow.presentation.calendar.CalendarScreen
import com.example.workshipflow.presentation.dashboard.DashboardScreen
import com.example.workshipflow.presentation.setlist.AddSongToSetlistScreen
import com.example.workshipflow.presentation.setlist.EditSetlistScreen
import com.example.workshipflow.presentation.setlist.SetlistDetailScreen
import com.example.workshipflow.presentation.songs.AddSongScreen
import com.example.workshipflow.presentation.songs.EditSongScreen
import com.example.workshipflow.presentation.songs.SongDetailScreen
import com.example.workshipflow.presentation.songs.SongListScreen
import com.example.workshipflow.ui.WorshipViewModel
import com.example.workshipflow.ui.theme.WorkshipFlowTheme
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkshipFlowTheme {
                WorshipApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorshipApp() {
    val navController = rememberNavController()
    val viewModel: WorshipViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.split("?")?.get(0)?.split("/")?.get(0)

    Scaffold(
        bottomBar = {
            if (currentRoute == "dashboard" || currentRoute == "songs" || currentRoute == "calendar") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "dashboard",
                        onClick = { if (currentRoute != "dashboard") navController.navigate("dashboard") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Songs") },
                        label = { Text("Library") },
                        selected = currentRoute == "songs",
                        onClick = { if (currentRoute != "songs") navController.navigate("songs") }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                        label = { Text("Schedule") },
                        selected = currentRoute == "calendar",
                        onClick = { if (currentRoute != "calendar") navController.navigate("calendar") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onLibraryClick = { navController.navigate("songs") },
                    onAddSongClick = { navController.navigate("addSong") },
                    onAddServiceClick = { navController.navigate("addService") },
                    onSongClick = { id -> navController.navigate("songDetail/$id") },
                    onUpcomingServiceClick = { id -> navController.navigate("setlistDetail/$id") }
                )
            }
            composable("songs") {
                SongListScreen(
                    viewModel = viewModel,
                    onSongClick = { id -> navController.navigate("songDetail/$id") },
                    onAddClick = { navController.navigate("addSong") }
                )
            }
            composable(
                "songDetail/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.LongType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getLong("songId") ?: return@composable
                SongDetailScreen(
                    songId = songId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("editSong/$id") }
                )
            }
            composable(
                "editSong/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.LongType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getLong("songId") ?: return@composable
                EditSongScreen(
                    songId = songId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("addSong") {
                AddSongScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onBrowserClick = { query ->
                        val route = if (query.isNullOrBlank()) "browser" else "browser?query=${URLEncoder.encode(query, "UTF-8")}"
                        navController.navigate(route)
                    },
                    onSaved = { id ->
                        navController.navigate("songDetail/$id") {
                            popUpTo("dashboard")
                        }
                    }
                )
            }
            composable(
                "browser?query={query}",
                arguments = listOf(navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query")
                BrowserScreen(
                    viewModel = viewModel,
                    initialQuery = query,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate("songDetail/$id") {
                            popUpTo("dashboard")
                        }
                    }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate("addService") },
                    onSetlistClick = { id -> navController.navigate("setlistDetail/$id") }
                )
            }
            composable("addService") {
                AddServiceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "setlistDetail/{setlistId}",
                arguments = listOf(navArgument("setlistId") { type = NavType.LongType })
            ) { backStackEntry ->
                val setlistId = backStackEntry.arguments?.getLong("setlistId") ?: return@composable
                SetlistDetailScreen(
                    setlistId = setlistId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddSong = { id -> navController.navigate("addSongToSetlist/$id") },
                    onSongClick = { id -> navController.navigate("songDetail/$id") },
                    onEditSetlist = { id -> navController.navigate("editSetlist/$id") }
                )
            }
            composable(
                "editSetlist/{setlistId}",
                arguments = listOf(navArgument("setlistId") { type = NavType.LongType })
            ) { backStackEntry ->
                val setlistId = backStackEntry.arguments?.getLong("setlistId") ?: return@composable
                EditSetlistScreen(
                    setlistId = setlistId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "addSongToSetlist/{setlistId}",
                arguments = listOf(navArgument("setlistId") { type = NavType.LongType })
            ) { backStackEntry ->
                val setlistId = backStackEntry.arguments?.getLong("setlistId") ?: return@composable
                AddSongToSetlistScreen(
                    setlistId = setlistId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
