package com.pondersource.androidsolidservices.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pondersource.androidsolidservices.ui.login.Login
import com.pondersource.androidsolidservices.ui.login.LoginViewModel
import com.pondersource.androidsolidservices.ui.main.Main
import com.pondersource.androidsolidservices.ui.main.MainViewModel
import com.pondersource.androidsolidservices.ui.startup.Startup
import com.pondersource.androidsolidservices.ui.startup.StartupViewModel
import kotlinx.serialization.Serializable

@Composable
fun ASSAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost (
        modifier = modifier,
        navController = navController,
        startDestination = Startup
    ) {
        composable<Startup> {
            Startup(navController, hiltViewModel<StartupViewModel>())
        }
        composable<Login> {
            Login(navController, hiltViewModel<LoginViewModel>())
        }
        composable<Main> {
            Main(navController, hiltViewModel<MainViewModel>())
        }
    }
}

@Serializable
object Startup

@Serializable
object Login

@Serializable
object Main