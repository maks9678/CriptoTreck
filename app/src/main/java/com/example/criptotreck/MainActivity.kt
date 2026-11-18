package com.example.criptotreck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
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
import com.example.criptotreck.ui.theme.CriptoTreckTheme
import com.example.criptotreck.ui.theme.CryptoRoute
import com.example.criptotreck.ui.theme.News.NewsScreen
import com.example.criptotreck.ui.theme.CryptoDetailRoute
import com.example.criptotreck.ui.theme.NewsListRoute
import com.example.criptotreck.ui.theme.crypto.CryptoScreen
import com.example.criptotreck.ui.theme.cryptoDetails.CryptoDetailsScreen

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                    }
                }
            )
            { paddingValues ->
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
                                    CryptoDetailRoute(
                                        id
                                    )
                                )
                            }
                        )
                    }
                    composable<CryptoDetailRoute> { backStackEntry ->
                        // Достаем переданный объект с аргументами
                        val route: CryptoDetailRoute = backStackEntry.toRoute()

                        CryptoDetailsScreen(
                            idCrypto = route.id,
                            onBackClick = {
                                if(backStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED)
                                navController.popBackStack()
                            })
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
        CryptoScreen{}

    }
}