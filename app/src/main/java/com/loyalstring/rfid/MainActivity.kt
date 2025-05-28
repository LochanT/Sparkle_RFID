package com.loyalstring.rfid

import android.annotation.SuppressLint
import android.content.Context
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.loyalstring.rfid.navigation.AppNavigation
import com.loyalstring.rfid.navigation.Screens
import com.loyalstring.rfid.navigation.listOfNavItems
import com.loyalstring.rfid.ui.theme.Purple40
import com.loyalstring.rfid.ui.theme.SparkleRFIDTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity  : ComponentActivity() {
    //private val viewModel: BulkProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SparkleRFIDTheme {
                SetupNavigation(baseContext)
            }
        }

        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.disableReaderMode(this)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SetupNavigation( context: Context) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }
    val scope = rememberCoroutineScope()
    var navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    ModalNavigationDrawer(

        {
            ModalDrawerSheet ( modifier = Modifier.background(Color.White),drawerContainerColor = Color.White ){
                Column {

                    Box(modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(0.8f)
                        .background(color = Purple40)) {
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
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(scrollState)
                    ) {
                        listOfNavItems.forEachIndexed { index, navigationItem ->
                            NavigationDrawerItem(
                                modifier = Modifier
                                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                                    .fillMaxWidth(0.7f),
                                label = {
                                    Text(
                                        text = navigationItem.title,
                                        fontSize = 16.sp,
                                        color = Color.DarkGray
                                    )
                                },
                                selected = index == selectedItemIndex,

                                onClick = {
                                    selectedItemIndex = index
                                    if (selectedItemIndex >= 2) {
                                        Toast.makeText(context, "Coming soon..", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        scope.launch {
                                            drawerState.close()
                                            navController.navigate(navigationItem.route)
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(navigationItem.selectedIcon),
                                        tint = Color.DarkGray,
                                        contentDescription = navigationItem.title
                                    )
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = Color.Transparent,
                                    unselectedContainerColor = Color.Transparent
                                ),


                                )
                        }
                    }
                }
            }
        }, drawerState = drawerState
    ) {



        Scaffold(
            topBar = {
                when (currentRoute) {
                    Screens.HomeScreen.route    -> HomeTopBar {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                    Screens.ProductManagementScreen.route -> ProductTopBar(navController)
                    else                  -> {

                    }  // no bar, or a default one
                }
            },
            content = { innerPadding ->
                AppNavigation(navController, drawerState, scope,context)

            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTopBar(navController: NavHostController) {
    TopAppBar(
        title = { Text("Product", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = {

                navController.navigate(Screens.HomeScreen.route) { // Navigate to HomeScreen
                    popUpTo(navController.graph.startDestinationId) // Clear back stack up to start destination
                    launchSingleTop = true // Avoid duplicate instances
                }

            }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
                )
            )
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onNavigationClick: () -> Unit) {
    TopAppBar(
        title = { Text("Home", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = {
                onNavigationClick()
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF5231A7), Color(0xFFD32940))
                )
            )
    )
}



