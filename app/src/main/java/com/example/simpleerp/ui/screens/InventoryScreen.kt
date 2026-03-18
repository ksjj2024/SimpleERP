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
import com.example.simpleerp.entity.Inventory
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen() {
    val db = SimpleERPApplication().database
    var inventory by remember { mutableStateOf<List<Inventory>>(emptyList()) }
    var showAdjustDialog by remember { mutableStateOf(false) }
    var selectedInventory by remember { mutableStateOf<Inventory?>(null) }
    var showLowStockOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        inventory = db.inventoryDao().getAllInventory().first()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdjustDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "调整库存")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("库存列表", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("仅显示低库存")
                    Switch(
                        checked = showLowStockOnly,
                        onCheckedChange = { showLowStockOnly = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredInventory = if (showLowStockOnly) {
                inventory.filter { it.quantity <= it.minStock }
            } else {
                inventory
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredInventory) { item ->
                    InventoryItem(
                        item = item,
                        onAdjust = {
                            selectedInventory = item
                            showAdjustDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryItem(item: Inventory, onAdjust: () -> Unit) {
    val isLowStock = item.quantity <= item.minStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isLowStock) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ) else CardDefaults.cardColors()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.titleMedium)
                Text("仓库: ${item.warehouse}", style = MaterialTheme.typography.bodySmall)
                if (item.minStock > 0) {
                    Text("最低库存: ${item.minStock}", style = MaterialTheme.typography.bodySmall)
                }
                if (item.maxStock > 0) {
                    Text("最高库存: ${item.maxStock}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "库存: ${item.quantity}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                if (isLowStock) {
                    Text("库存不足!", color = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onAdjust) {
                    Icon(Icons.Default.Edit, contentDescription = "调整")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustInventoryDialog(
    inventory: Inventory,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var quantity by remember { mutableStateOf(inventory.quantity.toString()) }
    var isIncrease by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调整库存") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("商品: ${inventory.productName}")
                Text("当前库存: ${inventory.quantity}")
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isIncrease,
                        onClick = { isIncrease = true }
                    )
                    Text("增加")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = !isIncrease,
                        onClick = { isIncrease = false }
                    )
                    Text("减少")
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("数量") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val newQuantity = if (isIncrease) {
                    inventory.quantity + (quantity.toDoubleOrNull() ?: 0.0)
                } else {
                    inventory.quantity - (quantity.toDoubleOrNull() ?: 0.0)
                }
                Text("新库存: $newQuantity")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = quantity.toDoubleOrNull() ?: 0.0
                    onConfirm(if (isIncrease) amount else -amount)
                },
                enabled = quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
