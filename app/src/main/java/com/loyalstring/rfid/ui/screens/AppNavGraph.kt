package com.loyalstring.rfid.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
         //   HomeScreenContentNew(modifier,navController)
        }
        composable("products") {
        //    ProductManagementScreen()
        }
    }
}
