package com.example.simpleerp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.simpleerp.SimpleERPApplication
import com.example.simpleerp.entity.Product
import com.example.simpleerp.entity.Customer
import com.example.simpleerp.entity.Inventory
import com.example.simpleerp.entity.SalesOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    val db = SimpleERPApplication().database
    
    var productCount by remember { mutableStateOf(0) }
    var customerCount by remember { mutableStateOf(0) }
    var inventoryCount by remember { mutableStateOf(0) }
    var totalSales by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        productCount = db.productDao().getAllProducts().first().size
        customerCount = db.customerDao().getAllCustomers().first().size
        inventoryCount = db.inventoryDao().getAllInventory().first().size
        val sales = db.salesOrderDao().getAllOrders().first()
        totalSales = sales.filter { it.status == "completed" }.sumOf { it.totalAmount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Welcome to SimpleERP",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Products",
                    value = productCount.toString(),
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("products") }
                )
                DashboardCard(
                    title = "Customers",
                    value = customerCount.toString(),
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("customers") }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Inventory",
                    value = inventoryCount.toString(),
                    icon = Icons.Default.Warehouse,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("inventory") }
                )
                DashboardCard(
                    title = "Sales",
                    value = "$${String.format("%.2f", totalSales)}",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("sales") }
                )
            }
        }

        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            QuickActionGrid(navController)
        }

        item {
            Text(
                "System Info",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SimpleERP v1.0")
                    Text("Offline ERP System")
                    Text("Features: Sales, Purchase, Inventory, Finance, Production, Reports")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun QuickActionGrid(navController: NavHostController) {
    val actions = listOf(
        Pair("New Purchase") { navController.navigate("purchase") },
        Pair("New Sale") { navController.navigate("sales") },
        Pair("Add Product") { navController.navigate("products") },
        Pair("Add Customer") { navController.navigate("customers") },
        Pair("Check Inventory") { navController.navigate("inventory") },
        Pair("View Reports") { navController.navigate("reports") }
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actions.take(3).forEach { (title, onClick) ->
                QuickActionButton(title, onClick, Modifier.weight(1f))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            actions.drop(3).forEach { (title, onClick) ->
                QuickActionButton(title, onClick, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun QuickActionButton(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(title)
    }
}
