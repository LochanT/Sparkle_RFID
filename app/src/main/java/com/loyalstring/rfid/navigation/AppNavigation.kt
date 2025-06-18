package com.loyalstring.rfid.navigation

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.loyalstring.rfid.ui.screens.AddProductScreen
import com.loyalstring.rfid.ui.screens.BulkProductScreen
import com.loyalstring.rfid.ui.screens.HomeScreen
import com.loyalstring.rfid.ui.screens.ImportExcelScreen
import com.loyalstring.rfid.ui.screens.LoginScreen
import com.loyalstring.rfid.ui.screens.ProductListScreen
import com.loyalstring.rfid.ui.screens.ProductManagementScreen
import com.loyalstring.rfid.ui.screens.ScanToDesktopScreen
import com.loyalstring.rfid.ui.screens.SettingsScreen
import com.loyalstring.rfid.ui.screens.SplashScreen
import com.loyalstring.rfid.ui.utils.UserPreferences
import kotlinx.coroutines.CoroutineScope


@Composable
fun AppNavigation(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    userPreferences: UserPreferences
) {
    NavHost(navController = navController, startDestination = Screens.SplashScreen.route) {

        composable("splash") {
            SplashScreen { nextRoute ->
                navController.popBackStack()
                navController.navigate(nextRoute)
            }
        }

        composable(Screens.HomeScreen.route) {
            HomeScreen(
                onBack = { navController.popBackStack() },
                navController,
                drawerState,
                scope
            )
        }
        composable(Screens.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(Screens.ProductManagementScreen.route) {
            ProductManagementScreen(
                onBack = { navController.popBackStack() },
                navController,
                userPreferences
            )
        }
        composable(Screens.AddProductScreen.route) {
            AddProductScreen(onBack = { navController.popBackStack() }, navController)

        }
        composable(Screens.BulkProductScreen.route) {
            BulkProductScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ImportExcelScreen.route) {
            ImportExcelScreen(onBack = { navController.popBackStack() }, navController)
        }
//        composable(Screens.ExportExcelScreen.route) {
//            ExportExcelScreen(onBack = { navController.popBackStack() }, navController)
//        }
        composable(Screens.ProductListScreen.route) {
            ProductListScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ScanToDesktopScreen.route) {
            ScanToDesktopScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.SettingsScreen.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                navController,
                userPreferences
            )
        }

    }
}

