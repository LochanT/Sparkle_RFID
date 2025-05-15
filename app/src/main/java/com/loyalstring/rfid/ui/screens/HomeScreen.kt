package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.loyalstring.rfid.navigation.NavItems
import com.loyalstring.rfid.navigation.listOfNavItems
import com.loyalstring.rfid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
  fun HomeScreen (
    onBack: () -> Unit,
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                        }
                    },

                    modifier = Modifier.background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
                        )
                    ),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent) // Example gradient color
                )
            },
            content = { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    Column(

                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(horizontal = 10.dp)

                        // Apply padding only horizontally
                    ){
                        val items = listOfNavItems.filter { it.title != "Home" }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items) { item ->
                                HomeGridCard(item,navController)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth() // Ensures the Box takes up the full width
                                .padding(start = 8.dp, top = 5.dp, bottom = 8.dp) // Adds padding to the Box
                                .height(30.dp) // Optional: You can set a specific height or remove this if you want it to adjust to the image's size
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.drawer_icon), // Replace with your actual image name
                                contentDescription = "Sparkle RFID Logo",
                                modifier = Modifier
                                    .align(Alignment.Center) // Centers the image within the Box
                            )
                        }
                    }
                }
            }
        )







    }

    @SuppressLint("SuspiciousIndentation")
    @Composable
    fun HomeGridCard(item: NavItems, navController: NavController) {
        val cardColors = SolidColor(Color.White)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(cardColors)
                        .fillMaxSize()
                        .clickable {
                            navController.navigate(item.route)
                        }
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = item.selectedIcon),
                            tint = Color.Unspecified,
                            contentDescription = item.title,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
                                ),

                            ),
                            text = item.title,

                            color = Color.Blue,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }



    }



