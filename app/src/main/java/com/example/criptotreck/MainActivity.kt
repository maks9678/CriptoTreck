package com.example.criptotreck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.criptotreck.navigation.CryptoDetailRoute
import com.example.criptotreck.navigation.CryptoRoute
import com.example.criptotreck.navigation.DemoRoute
import com.example.criptotreck.navigation.NewsListRoute
import com.example.criptotreck.ui.screens.demo.DemoScreen
import com.example.criptotreck.ui.screens.details.CryptoDetailsScreen
import com.example.criptotreck.ui.screens.market.CryptoScreen
import com.example.criptotreck.ui.screens.news.NewsScreen
import com.example.criptotreck.ui.theme.CriptoTreckTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute<NewsListRoute>() == true,
                            onClick = {
                                navController.navigate(NewsListRoute) {
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Новости") },
                            label = { Text("Новости") }
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute<CryptoRoute>() == true,
                            onClick = {
                                navController.navigate(CryptoRoute) {
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Рынок") },
                            label = { Text("Рынок") }
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute<DemoRoute>() == true,
                            onClick = {
                                navController.navigate(DemoRoute) {
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(Icons.Default.Apps, contentDescription = "Демо") },
                            label = { Text("Демо") }
                        )
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = NewsListRoute,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable<NewsListRoute> {
                        NewsScreen()
                    }
                    composable<CryptoRoute> {
                        CryptoScreen(
                            onCryptoClick = { id ->
                                navController.navigate(
                                    CryptoDetailRoute(id)
                                )
                            }
                        )
                    }
                    composable<CryptoDetailRoute> { backStackEntry ->
                        val route: CryptoDetailRoute = backStackEntry.toRoute()

                        CryptoDetailsScreen(
                            idCrypto = route.id,
                            onBackClick = {
                                if (backStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                    composable<DemoRoute> {
                        DemoScreen()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CriptoTreckTheme {
        CryptoScreen {}
    }
}
