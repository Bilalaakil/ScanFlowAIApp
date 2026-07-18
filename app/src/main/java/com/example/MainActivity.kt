package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.google.android.gms.ads.MobileAds
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.AIToolsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PdfToolsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}
        AdManager.loadInterstitialAd(this)
        AdManager.loadRewardedAd(this)
        
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(database.documentDao(), database.aiNoteDao())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScanFlowApp(repository)
                }
            }
        }
    }
}

@Composable
fun ScanFlowApp(repository: AppRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                repository = repository,
                onNavigateToAITools = { navController.navigate("ai_tools") },
                onNavigateToPdfTools = { navController.navigate("pdf_tools") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("ai_tools") {
            AIToolsScreen(
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }
        composable("pdf_tools") {
            PdfToolsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
