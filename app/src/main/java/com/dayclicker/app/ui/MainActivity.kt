package com.dayclicker.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dayclicker.app.ui.detail.CounterDetailScreen
import com.dayclicker.app.ui.edit.EditCounterScreen
import com.dayclicker.app.ui.home.HomeScreen
import com.dayclicker.app.ui.theme.DayClickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayClickerTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                onOpenCounter = { id -> nav.navigate("detail/$id") },
                onAddCounter = { nav.navigate("edit/-1") }
            )
        }
        composable("detail/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            CounterDetailScreen(
                counterId = id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("edit/$id") }
            )
        }
        composable("edit/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: -1L
            EditCounterScreen(
                counterId = if (id < 0) null else id,
                onDone = { nav.popBackStack() }
            )
        }
    }
}
