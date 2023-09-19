package com.example.biometricbanjo.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.biometricbanjo.home.ui.HomeScreen
import com.example.biometricbanjo.login.ui.LoginScreen
import com.example.biometricbanjo.main.navigation.Destinations
import com.example.biometricbanjo.ui.theme.BiometricBanjoTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val startDestination = Destinations.Login.name

            BiometricBanjoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold { scaffoldPadding ->
                        NavHost(
                            modifier = Modifier.padding(scaffoldPadding),
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable(Destinations.Home.name) {
                                HomeScreen(navHostController = navController)
                            }
                            composable(Destinations.Login.name) {
                                LoginScreen(navHostController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}
