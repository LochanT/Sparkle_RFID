package com.loyalstring.rfid.navigation

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.loyalstring.rfid.ui.screens.AddProductScreen
import com.loyalstring.rfid.ui.screens.BulkProductScreen
import com.loyalstring.rfid.ui.screens.ExportExcelScreen
import com.loyalstring.rfid.ui.screens.HomeScreen
import com.loyalstring.rfid.ui.screens.ImportExcelScreen
import com.loyalstring.rfid.ui.screens.ProductListScreen
import com.loyalstring.rfid.ui.screens.ProductManagementScreen
import kotlinx.coroutines.CoroutineScope


@Composable
fun AppNavigation(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context
) {
    NavHost(navController = navController, startDestination = Screens.HomeScreen.route) {
        composable(Screens.HomeScreen.route) {
            HomeScreen(
                onBack = { navController.popBackStack() },
                navController,
                drawerState,
                scope
            )
        }
        composable(Screens.ProductManagementScreen.route) {
            ProductManagementScreen(onBack = { navController.popBackStack() },navController)
        }
        composable(Screens.AddProductScreen.route) {
            AddProductScreen(onBack = { navController.popBackStack() },navController,context)

        }
        composable(Screens.BulkProductScreen.route) {
            BulkProductScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ImportExcelScreen.route) {
            ImportExcelScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ExportExcelScreen.route) {
            ExportExcelScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ProductListScreen.route) {
            ProductListScreen(onBack = { navController.popBackStack() }, navController)
        }

    }
}

