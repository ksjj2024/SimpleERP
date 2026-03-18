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
fun SalesOrderScreen() {
    val db = SimpleERPApplication().database
    var orders by remember { mutableStateOf<List<SalesOrder>>(emptyList()) }
    var customers by remember { mutableStateOf<List<Customer>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        orders = db.salesOrderDao().getAllOrders().first()
        customers = db.customerDao().getAllCustomers().first()
        products = db.productDao().getAllProducts().first()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "新建销售")
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
                SalesOrderItem(
                    order = order,
                    customerName = customers.find { it.id == order.customerId }?.name ?: "未知",
                    onStatusChange = { newStatus ->
                        orders = orders.map {
                            if (it.id == order.id) it.copy(status = newStatus) else it
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddSalesOrderDialog(
                customers = customers,
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
fun SalesOrderItem(
    order: SalesOrder,
    customerName: String,
    onStatusChange: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val remaining = order.totalAmount - order.paidAmount

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
            Text("客户: $customerName")
            Text("日期: ${dateFormat.format(Date(order.orderDate))}")
            Text("金额: ¥${String.format("%.2f", order.totalAmount)}")
            Text("已付: ¥${String.format("%.2f", order.paidAmount)}")
            if (remaining > 0) {
                Text("待收: ¥${String.format("%.2f", remaining)}", color = MaterialTheme.colorScheme.error)
            }
            if (order.remark.isNotEmpty()) {
                Text("备注: ${order.remark}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                if (order.status == "pending") {
                    TextButton(onClick = { onStatusChange("completed") }) {
                        Text("确认完成")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSalesOrderDialog(
    customers: List<Customer>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (SalesOrder) -> Unit
) {
    var orderNo by remember { mutableStateOf("SO${System.currentTimeMillis()}") }
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var orderDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var items by remember { mutableStateOf(listOf<Pair<Product, Double>>()) }
    var showProductPicker by remember { mutableStateOf(false) }

    val totalAmount = items.sumOf { it.first.salePrice * it.second }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建销售订单") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = orderNo,
                    onValueChange = { orderNo = it },
                    label = { Text("订单号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                var customerExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = customerExpanded,
                    onExpandedChange = { customerExpanded = it }
                ) {
                    OutlinedTextField(
                        value = customers.find { it.id == selectedCustomerId }?.name ?: "选择客户",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("客户") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = customerExpanded,
                        onDismissRequest = { customerExpanded = false }
                    ) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text(customer.name) },
                                onClick = {
                                    selectedCustomerId = customer.id
                                    customerExpanded = false
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
                    val order = SalesOrder(
                        orderNo = orderNo,
                        customerId = selectedCustomerId ?: 0,
                        orderDate = orderDate,
                        totalAmount = totalAmount,
                        status = "pending"
                    )
                    onConfirm(order)
                },
                enabled = selectedCustomerId != null && items.isNotEmpty()
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
