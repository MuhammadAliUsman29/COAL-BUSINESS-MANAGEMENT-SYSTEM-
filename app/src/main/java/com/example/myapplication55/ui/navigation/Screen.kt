package com.example.myapplication55.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Rounded.Dashboard)
    object UserManagement : Screen("user_management", "User Management", Icons.Rounded.Group)
    object Inventory : Screen("inventory", "Inventory", Icons.Rounded.Inventory)
    object Customers : Screen("customers", "Customers", Icons.Rounded.People)
    object Reports : Screen("reports", "Reports", Icons.Rounded.Assessment)
    object AdvancedReports : Screen("advanced_reports", "Advanced Reports", Icons.Rounded.Analytics)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
    object HelpCenter : Screen("help_center", "Help Center", Icons.AutoMirrored.Rounded.Help)
    object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy", Icons.Rounded.Security)
    object AboutUs : Screen("about_us", "About Us", Icons.Rounded.Info)
    object PdfArchive : Screen("pdf_archive", "All PDFs", Icons.Rounded.PictureAsPdf)
    object PurchaseHistory : Screen("purchase_history", "Purchases", Icons.Rounded.ShoppingCart)
    object CustomerDetail : Screen("customer_detail/{customerId}", "Customer Detail", Icons.Rounded.People)

    companion object {
        val sidebarItems = listOf(
            Dashboard,
            Inventory,
            Customers,
            PdfArchive,
            AdvancedReports,
            Settings,
            HelpCenter,
            PrivacyPolicy,
            AboutUs
        )
        val bottomBarItems = listOf(Dashboard, Inventory, Customers, Reports)
    }
}
