package com.loyalstring.rfid.navigation

import androidx.compose.foundation.layout.PaddingValues
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable

@Composable
fun MainScaffold(
    topBar: @Composable () -> Unit = {},
    //bottomBar: @Composable () -> Unit = {},
   // fab: @Composable () -> Unit = {},
   content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar    = topBar,
      ///  bottomBar = bottomBar,
      //  floatingActionButton = fab,
        content   = content
    )
}
