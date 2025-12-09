package com.example.chillstay.ui.profile

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.chillstay.ui.navigation.Routes
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext

object ProfileNavigation {
    fun route() = Routes.PROFILE
}

fun NavGraphBuilder.profileRoute(
    onLogoutClick: () -> Unit
) {
    composable(ProfileNavigation.route()) {
        val vm: ProfileViewModel = koinViewModel()
        val state = vm.uiState.collectAsStateWithLifecycle().value
        val context = LocalContext.current

        LaunchedEffect(vm) {
            vm.uiEffect.collect { effect ->
                when (effect) {
                    is ProfileUiEffect.ShowMessage -> Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        ProfileScreen(
            state = state,
            onEvent = vm::onEvent,
            onLogoutClick = onLogoutClick
        )
    }
}

fun NavHostController.navigateToProfile(
    navOptions: NavOptions? = null
) {
    navigate(
        route = ProfileNavigation.route(),
        navOptions = navOptions
    )
}

