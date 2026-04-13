package com.pondersource.androidsolidservices.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pondersource.androidsolidservices.ui.login.Login as LoginScreen
import com.pondersource.androidsolidservices.ui.login.LoginViewModel
import com.pondersource.androidsolidservices.ui.main.MainPage
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
            LoginScreen(navController, hiltViewModel<LoginViewModel>())
        }
        composable<MainPage> {
            MainPage(navController)
        }
    }
}

@Serializable
object Startup

@Serializable
data class Login(val isAddingAccount: Boolean = false)

@Serializable
object MainPage {

    @Serializable
    data class MainNavBottomItem<T: Any>(
        @field:StringRes val title: Int,
        @field:DrawableRes val icon: Int,
        val route: T,
    )

    @Serializable
    object AccessGrants

    @Serializable
    object Main

    @Serializable
    object Setting
}