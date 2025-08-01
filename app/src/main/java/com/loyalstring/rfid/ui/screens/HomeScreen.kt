package com.loyalstring.rfid.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.loyalstring.rfid.R
import com.loyalstring.rfid.navigation.NavItems
import com.loyalstring.rfid.navigation.listOfNavItems
import com.loyalstring.rfid.ui.utils.ToastUtils
import com.loyalstring.rfid.ui.utils.poppins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onBack: () -> Unit,
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val items = listOfNavItems.filter { it.title != "Home" && it.title != "Logout" }
    val context: Context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home", color = Color.White, fontFamily = poppins) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
                    )
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            val screenWidth = maxWidth
            val columns = when {
                screenWidth < 328.dp -> 2
                screenWidth < 400.dp -> 3
                else -> 4
            }

            val spacing = 16.dp
            val totalSpacing = spacing * (columns + 1)
            val cardSize = (screenWidth - totalSpacing) / columns

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .weight(1f),
                    columns = GridCells.Fixed(columns),
                    verticalArrangement = Arrangement.spacedBy(spacing),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    contentPadding = PaddingValues(top = 16.dp)
                ) {
                    items(items) { item ->
                        HomeGridCard(
                            item = item,
                            navController = navController,
                            size = cardSize,
                            context
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.drawer_icon),
                        contentDescription = "Sparkle RFID Logo",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


@Composable
fun HomeGridCard(item: NavItems, navController: NavController, size: Dp, context: Context) {
    Card(
        modifier = Modifier
            .size(size)
            .clickable {
                if (item.route.isNotEmpty()) {
                    navController.navigate(item.route)
                } else {
                    ToastUtils.showToast(context, "Coming Soon...")
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = item.selectedIcon),
                contentDescription = item.title,
                tint = Color.Unspecified,
                modifier = Modifier.size(size * 0.4f) // Scale icon size with card
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                fontFamily = poppins,
                maxLines = 2,
                lineHeight = 16.sp, // adds line spacing
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp) // optional to help with narrow cards
            )
        }
    }
}





