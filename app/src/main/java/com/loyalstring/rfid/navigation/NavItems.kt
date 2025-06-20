package com.loyalstring.rfid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.loyalstring.rfid.R

enum class Screens(val route: String) {
    SplashScreen("splash"),
    LoginScreen("login"),
    HomeScreen("home"),
    ProductManagementScreen("products"),
    AddProductScreen("add product"),
    BulkProductScreen("bulk products"),
    ImportExcelScreen("import excel"),
    ExportExcelScreen("export excel"),
    ProductListScreen("product list"),
    ScanToDesktopScreen("scan_web"),
    SettingsScreen("settings"),


}
data class NavItems (
    val title:String,
    val unselectedIcon: ImageVector,
    val selectedIcon: Int,
    val route:String
)
val listOfNavItems = listOf<NavItems>(
    NavItems(
        title = "Home",
        unselectedIcon= Icons.Outlined.Home,
        selectedIcon = ( R.drawable.home_svg),
        route = Screens.HomeScreen.route
    ),
    NavItems(
        title = "Product",
        unselectedIcon= Icons.Outlined.MailOutline,
        selectedIcon = ( R.drawable.product_gr_svg),
        route = Screens.ProductManagementScreen.route
    ),
    NavItems(
        title = "Inventory",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.inventory_gr_svg),
        route = ""
    ),
    NavItems(
        title = "Order",
        unselectedIcon= Icons.Outlined.Settings,
        selectedIcon = ( R.drawable.order_gr_svg),
        route = ""
    ),
    NavItems(
        title = "Search",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon =(R.drawable.search_gr_svg),
        route = ""

    ), NavItems(
        title = "Stock Transfer",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.stock_tr_gr_svg),
        route = ""

    ), NavItems(
        title = "Report",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.report_gr_svg),
        route = ""

    ),NavItems(
        title = "Quotations",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.quotation_gr_svg),
        route = ""

    ),NavItems(
        title = "Estimate",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.estimate_gr_svg),
        route = ""

    ),NavItems(
        title = "Invoice",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.invoice_gr_svg),
        route = ""

    ),NavItems(
        title = "Sample In",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon =( R.drawable.sample_in_gr_svg),
        route = ""

    ),NavItems(
        title = "Sample Out",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.sample_out_gr_svg),
        route = ""

    ),NavItems(
        title = "Settings",
        unselectedIcon= Icons.Outlined.FavoriteBorder,
        selectedIcon = ( R.drawable.setting_gr_svg),
        route = "settings"

    ),
    NavItems(
        title = "Logout",
        unselectedIcon = Icons.AutoMirrored.Default.Logout,
        selectedIcon = (R.drawable.logout),
        route = "login"

    ),

)
