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
fun PurchaseOrderScreen() {
    val db = SimpleERPApplication().database
    var orders by remember { mutableStateOf<List<PurchaseOrder>>(emptyList()) }
    var suppliers by remember { mutableStateOf<List<Supplier>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        orders = db.purchaseOrderDao().getAllOrders().first()
        suppliers = db.supplierDao().getAllSuppliers().first()
        products = db.productDao().getAllProducts().first()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "新建采购")
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
                PurchaseOrderItem(
                    order = order,
                    supplierName = suppliers.find { it.id == order.supplierId }?.name ?: "未知",
                    onStatusChange = { newStatus ->
                        orders = orders.map {
                            if (it.id == order.id) it.copy(status = newStatus) else it
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddPurchaseOrderDialog(
                suppliers = suppliers,
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
fun PurchaseOrderItem(
    order: PurchaseOrder,
    supplierName: String,
    onStatusChange: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("订单号: ${order.orderNo}", style = MaterialTheme.typography.titleMedium)
                StatusChip(order.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("供应商: $supplierName")
            Text("日期: ${dateFormat.format(Date(order.orderDate))}")
            Text("金额: ¥${String.format("%.2f", order.totalAmount)}")
            if (order.remark.isNotEmpty()) {
                Text("备注: ${order.remark}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                if (order.status == "pending") {
                    TextButton(onClick = { onStatusChange("received") }) {
                        Text("确认收货")
                    }
                }
                if (order.status != "cancelled") {
                    TextButton(onClick = { onStatusChange("cancelled") }) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, text) = when (status) {
        "pending" -> Pair(MaterialTheme.colorScheme.secondary, "待处理")
        "received" -> Pair(MaterialTheme.colorScheme.primary, "已完成")
        "completed" -> Pair(MaterialTheme.colorScheme.primary, "已完成")
        "cancelled" -> Pair(MaterialTheme.colorScheme.error, "已取消")
        else -> Pair(MaterialTheme.colorScheme.tertiary, status)
    }
    Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseOrderDialog(
    suppliers: List<Supplier>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (PurchaseOrder) -> Unit
) {
    var orderNo by remember { mutableStateOf("PO${System.currentTimeMillis()}") }
    var selectedSupplierId by remember { mutableStateOf<Long?>(null) }
    var orderDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var items by remember { mutableStateOf(listOf<Pair<Product, Double>>()) }
    var showProductPicker by remember { mutableStateOf(false) }

    val totalAmount = items.sumOf { it.first.salePrice * it.second }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建采购订单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = orderNo,
                    onValueChange = { orderNo = it },
                    label = { Text("订单号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Supplier dropdown
                var supplierExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = supplierExpanded,
                    onExpandedChange = { supplierExpanded = it }
                ) {
                    OutlinedTextField(
                        value = suppliers.find { it.id == selectedSupplierId }?.name ?: "选择供应商",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("供应商") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = supplierExpanded,
                        onDismissRequest = { supplierExpanded = false }
                    ) {
                        suppliers.forEach { supplier ->
                            DropdownMenuItem(
                                text = { Text(supplier.name) },
                                onClick = {
                                    selectedSupplierId = supplier.id
                                    supplierExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("商品明细:")
                items.forEach { (product, quantity) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${product.name} x $quantity")
                        Text("¥${product.salePrice * quantity}")
                    }
                }

                OutlinedButton(
                    onClick = { showProductPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Text("添加商品")
                }

                Text("合计: ¥${String.format("%.2f", totalAmount)}")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val order = PurchaseOrder(
                        orderNo = orderNo,
                        supplierId = selectedSupplierId ?: 0,
                        orderDate = orderDate,
                        totalAmount = totalAmount,
                        status = "pending"
                    )
                    onConfirm(order)
                },
                enabled = selectedSupplierId != null && items.isNotEmpty()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    if (showProductPicker) {
        ProductPickerDialog(
            products = products,
            onDismiss = { showProductPicker = false },
            onSelect = { product, quantity ->
                items = items + (product to quantity)
                showProductPicker = false
            }
        )
    }
}

@Composable
fun ProductPickerDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onSelect: (Product, Double) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择商品") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                products.forEach { product ->
                    OutlinedButton(
                        onClick = { selectedProduct = product },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${product.name} - ¥${product.salePrice}")
                    }
                }

                if (selectedProduct != null) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("数量") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedProduct?.let {
                        onSelect(it, quantity.toDoubleOrNull() ?: 1.0)
                    }
                },
                enabled = selectedProduct != null
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
