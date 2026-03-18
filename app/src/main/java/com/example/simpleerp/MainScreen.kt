package com.example.simpleerp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.simpleerp.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Dashboard : Screen("dashboard", "Home", { Icon(Icons.Default.Home, null) })
    object Products : Screen("products", "Products", { Icon(Icons.Default.Inventory, null) })
    object Customers : Screen("customers", "Customers", { Icon(Icons.Default.People, null) })
    object Suppliers : Screen("suppliers", "Suppliers", { Icon(Icons.Default.LocalShipping, null) })
    object Purchase : Screen("purchase", "Purchase", { Icon(Icons.Default.ShoppingCart, null) })
    object Sales : Screen("sales", "Sales", { Icon(Icons.Default.PointOfSale, null) })
    object Inventory : Screen("inventory", "Inventory", { Icon(Icons.Default.Warehouse, null) })
    object Finance : Screen("finance", "Finance", { Icon(Icons.Default.AccountBalance, null) })
    object Production : Screen("production", "Production", { Icon(Icons.Default.Factory, null) })
    object Reports : Screen("reports", "Reports", { Icon(Icons.Default.BarChart, null) })
}

val screens = listOf(
    Screen.Dashboard,
    Screen.Products,
    Screen.Customers,
    Screen.Suppliers,
    Screen.Purchase,
    Screen.Sales,
    Screen.Inventory,
    Screen.Finance,
    Screen.Production,
    Screen.Reports
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        val currentScreen = screens.find { it.route == currentRoute }
                        Text(currentScreen?.title ?: "SimpleERP")
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen(navController) }
                composable(Screen.Products.route) { ProductScreen() }
                composable(Screen.Customers.route) { CustomerScreen() }
                composable(Screen.Suppliers.route) { SupplierScreen() }
                composable(Screen.Purchase.route) { PurchaseOrderScreen() }
                composable(Screen.Sales.route) { SalesOrderScreen() }
                composable(Screen.Inventory.route) { InventoryScreen() }
                composable(Screen.Finance.route) { FinanceScreen() }
                composable(Screen.Production.route) { ProductionScreen() }
                composable(Screen.Reports.route) { ReportScreen() }
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "SimpleERP",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        screens.forEach { screen ->
            NavigationDrawerItem(
                icon = screen.icon,
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
