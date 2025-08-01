package com.loyalstring.rfid.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.loyalstring.rfid.data.local.entity.BulkItem
import com.loyalstring.rfid.data.model.order.CustomOrderResponse
import com.loyalstring.rfid.ui.screens.AddProductScreen
import com.loyalstring.rfid.ui.screens.BulkProductScreen
import com.loyalstring.rfid.ui.screens.EditProductScreen
import com.loyalstring.rfid.ui.screens.HomeScreen
import com.loyalstring.rfid.ui.screens.ImportExcelScreen
import com.loyalstring.rfid.ui.screens.InventoryMenuScreen
import com.loyalstring.rfid.ui.screens.InvoiceScreen
import com.loyalstring.rfid.ui.screens.LoginScreen
import com.loyalstring.rfid.ui.screens.OrderLisrScreen

import com.loyalstring.rfid.ui.screens.OrderScreen
import com.loyalstring.rfid.ui.screens.ProductListScreen
import com.loyalstring.rfid.ui.screens.ProductManagementScreen
import com.loyalstring.rfid.ui.screens.ScanBoxScreeen
import com.loyalstring.rfid.ui.screens.ScanBranchScreen
import com.loyalstring.rfid.ui.screens.ScanCounterScreen
import com.loyalstring.rfid.ui.screens.ScanDisplayScreen
import com.loyalstring.rfid.ui.screens.ScanExhibitionScreen
import com.loyalstring.rfid.ui.screens.ScanToDesktopScreen
import com.loyalstring.rfid.ui.screens.SearchScreen
import com.loyalstring.rfid.ui.screens.SettingsScreen
import com.loyalstring.rfid.ui.screens.SplashScreen
import com.loyalstring.rfid.ui.screens.StockTransferScreen
import com.loyalstring.rfid.ui.utils.UserPreferences
import kotlinx.coroutines.CoroutineScope


@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
        composable(Screens.InventoryMenuScreen.route) {
            InventoryMenuScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ScanDisplayScreen.route) {
            ScanDisplayScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ScanCounterScreen.route) {
            ScanCounterScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ScanBranchScreen.route) {
            ScanBranchScreen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.ScanExhibitionScreen.route) {
            ScanExhibitionScreen(onBack = { navController.popBackStack() }, navController)
        }

        composable(Screens.ScanBoxScreen.route) {
            ScanBoxScreeen(onBack = { navController.popBackStack() }, navController)
        }
        composable(Screens.SearchScreen.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }
        composable(Screens.StockTransferScreen.route) {
            StockTransferScreen(onBack = { navController.popBackStack() }, navController)
        }

        composable(Screens.EditProductScreen.route) { backStackEntry ->
            val item = navController.previousBackStackEntry?.savedStateHandle?.get<BulkItem>("item")

            item?.let {
                EditProductScreen(
                    onBack = { navController.popBackStack() },
                    navController = navController,
                    item = it
                )
            } /*?: run {
                Text("Error: No item passed")
            }*/
        }






        composable(Screens.SettingsScreen.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                navController,
                userPreferences
            )
        }
        composable(Screens.OrderScreen.route) {
            OrderScreen(
                onBack = { navController.popBackStack() },
                navController,
                userPreferences
            )
        }

      /*  composable(Screens.InvoiceScreen.route) {
            InvoiceScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                item = it

            )

        }*/

        composable(Screens.InvoiceScreen.route) { backStackEntry ->
            val item = navController.previousBackStackEntry?.savedStateHandle?.get<CustomOrderResponse>("customerOrderResponse")

            item?.let {
                InvoiceScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    item = it
                )
            } /*?: run {
                Text("Error: No item passed")
            }*/
        }

        composable(Screens.OrderListScreen.route) {
            OrderLisrScreen(
                onBack = { navController.popBackStack() },
                navController,
                userPreferences
            )
        }


    }
}



