package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.loyalstring.rfid.navigation.listOfNavItems
import com.loyalstring.rfid.R
import com.loyalstring.rfid.ui.theme.Purple40
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppDrawer() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val scope = rememberCoroutineScope()
    var navController = rememberNavController()

    ModalNavigationDrawer(

        {
            ModalDrawerSheet {
                Column {
                    Box(modifier = Modifier.height(100.dp).fillMaxWidth(0.8f).background(color = Purple40)) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rounded app icon
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with your app icon resource
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                                    .padding(8.dp)
                            )
                            // Spacing between icon and app name
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Sparkle RFID",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    listOfNavItems.forEachIndexed { index, navigationItem ->
                        NavigationDrawerItem(
                            label = { Text(text = navigationItem.title) },
                            selected = index == selectedItemIndex,
                            onClick = {
                                selectedItemIndex = index
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate(navigationItem.route)
                                }
                            },
                            icon = {
//                                Icon(
//                                    imageVector = (if (index == selectedItemIndex) {
//                                        navigationItem.selectedIcon
//                                    } else {
//                                        navigationItem.unselectedIcon
//                                    }) as ImageVector, contentDescription = navigationItem.title
//                                )
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .fillMaxWidth(0.7f)
                        )
                    }
                }
            }
        }, drawerState = drawerState
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Home " +
                                "")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }

                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )

            },
            content = {
                // Add NavHost here
               // AppNavigation(navController)
            },

            )
    }
}