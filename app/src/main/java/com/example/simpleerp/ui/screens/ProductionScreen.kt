package com.example.simpleerp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simpleerp.SimpleERPApplication
import com.example.simpleerp.entity.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen() {
    val db = SimpleERPApplication().database
    var orders by remember { mutableStateOf<List<ProductionOrder>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        orders = db.productionOrderDao().getAllOrders().first()
        products = db.productDao().getAllProducts().first()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "鏂板缓鐢熶骇")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(orders) { order ->
                ProductionOrderItem(
                    order = order,
                    onStatusChange = { status, quantity ->
                        orders = orders.map {
                            if (it.id == order.id) it.copy(status = status, completedQuantity = quantity) else it
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddProductionOrderDialog(
                products = products,
                onDismiss = { showAddDialog = false },
                onConfirm = { order ->
                    orders = orders + order
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ProductionOrderItem(
    order: ProductionOrder,
    onStatusChange: (String, Double) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val progress = if (order.plannedQuantity > 0) {
        (order.completedQuantity / order.plannedQuantity * 100).toInt()
    } else 0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("璁㈠崟鍙? ${order.orderNo}", style = MaterialTheme.typography.titleMedium)
                ProductionStatusChip(order.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("浜у搧: ${order.productName}")
            Text("璁″垝鏁伴噺: ${order.plannedQuantity}")
            Text("瀹屾垚鏁伴噺: ${order.completedQuantity}")
            Text("寮€濮嬫棩鏈? ${dateFormat.format(Date(order.startDate))}")
            order.endDate?.let {
                Text("缁撴潫鏃ユ湡: ${dateFormat.format(Date(it))}")
            }
            if (order.remark.isNotEmpty()) {
                Text("澶囨敞: ${order.remark}")
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${progress}%", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                if (order.status == "planned") {
                    TextButton(onClick = { onStatusChange("in_progress", order.completedQuantity) }) {
                        Text("寮€濮嬬敓浜?)
                    }
                }
                if (order.status == "in_progress") {
                    TextButton(onClick = { 
                        onStatusChange("completed", order.plannedQuantity)
                    }) {
                        Text("瀹屾垚")
                    }
                }
                if (order.status != "completed" && order.status != "cancelled") {
                    TextButton(onClick = { onStatusChange("cancelled", order.completedQuantity) }) {
                        Text("鍙栨秷")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductionStatusChip(status: String) {
    val (color, text) = when (status) {
        "planned" -> Pair(MaterialTheme.colorScheme.secondary, "璁″垝涓?)
        "in_progress" -> Pair(MaterialTheme.colorScheme.primary, "鐢熶骇涓?)
        "completed" -> Pair(MaterialTheme.colorScheme.primary, "宸插畬鎴?)
        "cancelled" -> Pair(MaterialTheme.colorScheme.error, "宸插彇娑?)
        else -> Pair(MaterialTheme.colorScheme.tertiary, status)
    }
    Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductionOrderDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (ProductionOrder) -> Unit
) {
    var orderNo by remember { mutableStateOf("PD${System.currentTimeMillis()}") }
    var selectedProductId by remember { mutableStateOf<Long?>(null) }
    var plannedQuantity by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var remark by remember { mutableStateOf("") }
    var productExpanded by remember { mutableStateOf(false) }

    val selectedProduct = products.find { it.id == selectedProductId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("鏂板缓鐢熶骇璁㈠崟") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = orderNo,
                    onValueChange = { orderNo = it },
                    label = { Text("璁㈠崟鍙?) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "閫夋嫨浜у搧",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("浜у搧") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name) },
                                onClick = {
                                    selectedProductId = product.id
                                    productExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = plannedQuantity,
                    onValueChange = { plannedQuantity = it },
                    label = { Text("璁″垝鏁伴噺 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("澶囨敞") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val product = products.find { it.id == selectedProductId }
                    val order = ProductionOrder(
                        orderNo = orderNo,
                        productId = selectedProductId ?: 0,
                        productName = product?.name ?: "",
                        plannedQuantity = plannedQuantity.toDoubleOrNull() ?: 0.0,
                        startDate = startDate,
                        status = "planned",
                        remark = remark
                    )
                    onConfirm(order)
                },
                enabled = selectedProductId != null && plannedQuantity.toDoubleOrNull() != null
            ) {
                Text("淇濆瓨")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("鍙栨秷")
            }
        }
    )
}
