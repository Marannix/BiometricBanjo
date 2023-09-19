@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)

package com.example.biometricbanjo.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.example.biometricbanjo.R
import com.example.biometricbanjo.common.composable.CallToActionButton
import com.example.biometricbanjo.common.composable.lottie.LottieAsset
import com.example.biometricbanjo.login.ui.LoginContract
import com.example.biometricbanjo.main.navigation.Destinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        navHostController = navHostController,
        uiState = uiState,
        uiEvents = viewModel::onUiEvent
    )
}

@ExperimentalMaterial3Api
@Composable
fun HomeContent(
    navHostController: NavHostController,
    uiState: HomeContract.UiState,
    uiEvents: (HomeContract.UiEvents) -> Unit,
) {

    if (!uiState.loggedIn) {
        LaunchedEffect(key1 = Unit) {
            navHostController.navigate(
                route = Destinations.Login.name,
                navOptions = NavOptions
                    .Builder()
                    .setPopUpTo(Destinations.Home.name, true)
                    .build()
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.titleLarge,
                        text = "Home Screen",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                colors = androidx.compose.material3.TopAppBarDefaults.smallTopAppBarColors()
            )
        },
        content = { contentPadding ->

            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Welcome")

                Spacer(modifier = Modifier.height(8.dp))

                LottieAsset(lottieRes = R.raw.cat_sleeping)

                Spacer(modifier = Modifier.weight(1f))

                CallToActionButton(
                    label = stringResource(id = R.string.log_out)
                ) {
                    uiEvents(HomeContract.UiEvents.LogoutClicked)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    )
}
