package com.example.myapplication55

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication55.ui.navigation.Screen
import com.example.myapplication55.ui.screens.auth.AuthViewModel
import com.example.myapplication55.ui.screens.auth.LoginScreen
import com.example.myapplication55.ui.screens.auth.SignUpScreen
import com.example.myapplication55.ui.screens.customers.CustomerDetailScreen
import com.example.myapplication55.ui.screens.customers.CustomerDetailViewModel
import com.example.myapplication55.ui.screens.customers.CustomersScreen
import com.example.myapplication55.ui.screens.customers.CustomersViewModel
import com.example.myapplication55.ui.screens.dashboard.DashboardScreen
import com.example.myapplication55.ui.screens.dashboard.DashboardViewModel
import com.example.myapplication55.ui.screens.inventory.InventoryScreen
import com.example.myapplication55.ui.screens.inventory.InventoryViewModel
import com.example.myapplication55.ui.screens.inventory.PurchaseHistoryScreen
import com.example.myapplication55.ui.screens.profile.AccountSettingsScreen
import com.example.myapplication55.ui.screens.profile.ProfileScreen
import com.example.myapplication55.ui.screens.profile.ProfileViewModel
import com.example.myapplication55.ui.screens.profile.AboutUsScreen
import com.example.myapplication55.ui.screens.profile.HelpCenterScreen
import com.example.myapplication55.ui.screens.profile.PrivacyPolicyScreen
import com.example.myapplication55.ui.screens.profile.UserManagementScreen
import com.example.myapplication55.ui.screens.reports.ReportsScreen
import com.example.myapplication55.ui.screens.reports.ReportsViewModel
import com.example.myapplication55.ui.screens.archive.PdfArchiveScreen
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication55.data.local.entities.User
import com.example.myapplication55.ui.theme.CoalPrimary
import com.example.myapplication55.ui.theme.CoalSurface
import com.example.myapplication55.ui.theme.MyApplication55Theme
import com.example.myapplication55.ui.theme.ThemeManager
import com.example.myapplication55.ui.theme.ProfessionalBlue
import com.example.myapplication55.ui.theme.ErrorRed
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication55Theme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory((LocalContext.current.applicationContext as CmsApplication).repository)
    )
) {
    val isAuthenticated by authViewModel.isAuthenticated
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val repository = (context.applicationContext as CmsApplication).repository
    val navController = rememberNavController()
    var showAdminOversight by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.checkAndPrepopulate()
    }

    if (showAdminOversight) {
        val profileViewModel: ProfileViewModel = viewModel(
            factory = ProfileViewModel.Factory(repository)
        )
        UserManagementScreen(viewModel = profileViewModel, onBack = { showAdminOversight = false })
        return
    }

    if (!isAuthenticated) {
        var isSignUp by remember { mutableStateOf(false) }
        if (isSignUp) {
            SignUpScreen(authViewModel, onLogin = { isSignUp = false })
        } else {
            LoginScreen(authViewModel, 
                onSignUp = { isSignUp = true },
                onAdminOversight = { showAdminOversight = true }
            )
        }
        return
    }

    MainLayout(
        currentUser = currentUser,
        navController = navController,
        onLogout = { authViewModel.logout() }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(repository)
                )
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onAddCustomer = { navController.navigate(Screen.Customers.route) },
                    onAddStock = { navController.navigate(Screen.Inventory.route) },
                    onViewAllCustomers = { navController.navigate(Screen.Customers.route) },
                    onViewReports = { navController.navigate(Screen.Reports.route) },
                    onViewInventory = { navController.navigate(Screen.Inventory.route) }
                )
            }
            composable(Screen.Inventory.route) {
                val inventoryViewModel: InventoryViewModel = viewModel(
                    factory = InventoryViewModel.Factory(repository)
                )
                InventoryScreen(inventoryViewModel, onViewPurchaseHistory = {
                    navController.navigate(Screen.PurchaseHistory.route)
                })
            }
            composable(Screen.PurchaseHistory.route) {
                val inventoryViewModel: InventoryViewModel = viewModel(
                    factory = InventoryViewModel.Factory(repository)
                )
                PurchaseHistoryScreen(inventoryViewModel, onBack = {
                    navController.popBackStack()
                })
            }
            composable(Screen.Customers.route) {
                val customersViewModel: CustomersViewModel = viewModel(
                    factory = CustomersViewModel.Factory(repository)
                )
                CustomersScreen(customersViewModel, onCustomerClick = { id ->
                    navController.navigate("customer_detail/$id")
                })
            }
            composable(Screen.Reports.route) {
                val reportsViewModel: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(repository)
                )
                ReportsScreen(reportsViewModel, onOpenGallery = {
                    navController.navigate(Screen.PdfArchive.route)
                })
            }
            composable(Screen.PdfArchive.route) {
                val reportsViewModel: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(repository)
                )
                PdfArchiveScreen(reportsViewModel, onBack = {
                    navController.popBackStack()
                })
            }
            composable(Screen.AboutUs.route) {
                AboutUsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.HelpCenter.route) {
                HelpCenterScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.UserManagement.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(repository)
                )
                UserManagementScreen(viewModel = profileViewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.AdvancedReports.route) {
                val reportsViewModel: ReportsViewModel = viewModel(
                    factory = ReportsViewModel.Factory(repository)
                )
                ReportsScreen(reportsViewModel, onOpenGallery = {
                    navController.navigate(Screen.PdfArchive.route)
                })
            }
            composable(Screen.Settings.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(repository)
                )
                AccountSettingsScreen(viewModel = profileViewModel, onBack = { navController.popBackStack() })
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(repository)
                )
                ProfileScreen(profileViewModel)
            }
            composable("account_settings") {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(repository)
                )
                AccountSettingsScreen(viewModel = profileViewModel, onBack = {
                    navController.popBackStack()
                })
            }
            composable(
                route = Screen.CustomerDetail.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                val customerDetailViewModel: CustomerDetailViewModel = viewModel(
                    factory = CustomerDetailViewModel.Factory(repository, customerId)
                )
                CustomerDetailScreen(customerDetailViewModel, onBack = {
                    navController.popBackStack()
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    currentUser: User?,
    navController: androidx.navigation.NavHostController,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    var showFabMenu by remember { mutableStateOf(false) }
    var showProfileDropdown by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerTonalElevation = 0.dp
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "CMS ENGINE",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CoalPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(Icons.Default.Close, contentDescription = "Close Menu", tint = Color.Gray)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                    Screen.sidebarItems.forEach { screen ->
                        NavigationDrawerItem(
                            label = { Text(screen.title.uppercase()) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = CoalPrimary.copy(alpha = 0.1f),
                                selectedIconColor = CoalPrimary,
                                selectedTextColor = CoalPrimary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("CMS ENGINE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall) },
                    navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                },
                    actions = {
                        Box {
                            IconButton(onClick = { showProfileDropdown = true }) {
                                Surface(
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = CoalSurface,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    if (currentUser?.profilePicPath != null) {
                                        coil.compose.AsyncImage(
                                            model = currentUser!!.profilePicPath,
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showProfileDropdown,
                                onDismissRequest = { showProfileDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("View/Edit Profile", color = ProfessionalBlue) },
                                    onClick = { 
                                        showProfileDropdown = false 
                                        navController.navigate("profile")
                                    },
                                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = ProfessionalBlue) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Account Settings", color = ProfessionalBlue) },
                                    onClick = { 
                                        showProfileDropdown = false 
                                        navController.navigate("account_settings")
                                    },
                                    leadingIcon = { Icon(Icons.Rounded.ManageAccounts, contentDescription = null, tint = ProfessionalBlue) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Change Password", color = ProfessionalBlue) },
                                    onClick = { 
                                        showProfileDropdown = false 
                                        navController.navigate("account_settings")
                                    },
                                    leadingIcon = { Icon(Icons.Rounded.Password, contentDescription = null, tint = ProfessionalBlue) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                DropdownMenuItem(
                                    text = { Text("Logout", color = ErrorRed) },
                                    onClick = { 
                                        showProfileDropdown = false 
                                        onLogout()
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = ErrorRed) }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = CoalPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CoalSurface,
                    contentColor = Color.Gray
                ) {
                    Screen.bottomBarItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentDestination?.route != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CoalPrimary,
                                selectedTextColor = CoalPrimary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentDestination?.route == Screen.Dashboard.route) {
                    Column(horizontalAlignment = Alignment.End) {
                        AnimatedVisibility(
                            visible = showFabMenu,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                ExtendedFloatingActionButton(
                                    onClick = { 
                                        showFabMenu = false
                                        navController.navigate(Screen.Customers.route)
                                    },
                                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                                    text = { Text("ADD CUSTOMER") },
                                    containerColor = Color(0xFF1A237E),
                                    contentColor = Color.White
                                )
                                ExtendedFloatingActionButton(
                                    onClick = { 
                                        showFabMenu = false
                                        navController.navigate(Screen.Inventory.route)
                                    },
                                    icon = { Icon(Icons.Default.PostAdd, contentDescription = null) },
                                    text = { Text("ADD STOCK") },
                                    containerColor = Color(0xFF1A237E),
                                    contentColor = Color.White
                                )
                            }
                        }
                        FloatingActionButton(
                            onClick = { showFabMenu = !showFabMenu },
                            containerColor = CoalPrimary,
                            contentColor = Color.Black
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    MyApplication55Theme {
        MainLayout(
            currentUser = User(
                id = 1,
                fullName = "John Doe",
                cnic = "12345-6789012-3",
                mobileNumber = "03001234567",
                passwordHash = "password",
                isAdmin = true
            ),
            navController = navController,
            onLogout = {}
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Dashboard Content Placeholder")
            }
        }
    }
}
