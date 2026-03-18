package com.example.simpleerp.ui.screens

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.simpleerp.SimpleERPApplication
import com.example.simpleerp.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen() {
    val db = SimpleERPApplication().database
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var salesOrders by remember { mutableStateOf<List<SalesOrder>>(emptyList()) }
    var purchaseOrders by remember { mutableStateOf<List<PurchaseOrder>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var inventory by remember { mutableStateOf<List<Inventory>>(emptyList()) }

    var selectedReportType by remember { mutableStateOf("sales") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var isExporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }

    val reportTypes = listOf(
        "sales" to "Sales Report",
        "purchase" to "Purchase Report",
        "finance" to "Finance Report",
        "inventory" to "Inventory Report"
    )

    LaunchedEffect(Unit) {
        salesOrders = db.salesOrderDao().getAllOrders().first()
        purchaseOrders = db.purchaseOrderDao().getAllOrders().first()
        transactions = db.transactionDao().getAllTransactions().first()
        inventory = db.inventoryDao().getAllInventory().first()
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val filteredSales = salesOrders.filter { 
        it.orderDate in startDate..endDate && it.status != "cancelled"
    }
    val filteredPurchase = purchaseOrders.filter { 
        it.orderDate in startDate..endDate && it.status != "cancelled"
    }
    val filteredTransactions = transactions.filter { 
        it.transactionDate in startDate..endDate
    }

    val totalSales = filteredSales.sumOf { it.totalAmount }
    val totalPurchase = filteredPurchase.sumOf { it.totalAmount }
    val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    val profit = totalIncome - totalExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Reports", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Date Range", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                startDate = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                                endDate = System.currentTimeMillis()
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("7 Days") }
                        OutlinedButton(
                            onClick = {
                                startDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                                endDate = System.currentTimeMillis()
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("30 Days") }
                        OutlinedButton(
                            onClick = {
                                startDate = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
                                endDate = System.currentTimeMillis()
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("1 Year") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("From: ${dateFormat.format(Date(startDate))} To: ${dateFormat.format(Date(endDate))}")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Type", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reportTypes.forEach { (type, label) ->
                            FilterChip(
                                selected = selectedReportType == type,
                                onClick = { selectedReportType = type },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        reportTypes.find { it.first == selectedReportType }?.second ?: "Report",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedReportType) {
                        "sales" -> {
                            StatRow("Orders", "${filteredSales.size}")
                            StatRow("Total", "$${String.format("%.2f", totalSales)}")
                            StatRow("Average", "$${if (filteredSales.isNotEmpty()) String.format("%.2f", totalSales / filteredSales.size) else "0.00"}")
                        }
                        "purchase" -> {
                            StatRow("Orders", "${filteredPurchase.size}")
                            StatRow("Total", "$${String.format("%.2f", totalPurchase)}")
                            StatRow("Average", "$${if (filteredPurchase.isNotEmpty()) String.format("%.2f", totalPurchase / filteredPurchase.size) else "0.00"}")
                        }
                        "finance" -> {
                            StatRow("Income", "$${String.format("%.2f", totalIncome)}")
                            StatRow("Expense", "$${String.format("%.2f", totalExpense)}")
                            StatRow("Profit", "$${String.format("%.2f", profit)}")
                        }
                        "inventory" -> {
                            StatRow("Products", "${inventory.size}")
                            StatRow("Total Qty", "${inventory.sumOf { it.quantity }}")
                            val lowStock = inventory.filter { it.quantity <= it.minStock }.size
                            StatRow("Low Stock", "$lowStock")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (exportMessage.isNotEmpty()) {
                        Text(exportMessage, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isExporting = true
                                try {
                                    val file = exportToCSV(
                                        context,
                                        selectedReportType,
                                        startDate,
                                        endDate,
                                        salesOrders,
                                        purchaseOrders,
                                        transactions,
                                        inventory
                                    )
                                    exportMessage = "Exported: ${file.name}"
                                } catch (e: Exception) {
                                    exportMessage = "Error: ${e.message}"
                                }
                                isExporting = false
                            }
                        },
                        enabled = !isExporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export CSV")
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

suspend fun exportToCSV(
    context: Context,
    reportType: String,
    startDate: Long,
    endDate: Long,
    salesOrders: List<SalesOrder>,
    purchaseOrders: List<PurchaseOrder>,
    transactions: List<Transaction>,
    inventory: List<Inventory>
): File = withContext(Dispatchers.IO) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fileName = "ERP_${reportType}_${System.currentTimeMillis()}.csv"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
    
    val content = buildString {
        when (reportType) {
            "sales" -> {
                appendLine("OrderNo,Date,Amount,Status")
                val filtered = salesOrders.filter { it.orderDate in startDate..endDate }
                filtered.forEach { order ->
                    appendLine("${order.orderNo},${dateFormat.format(Date(order.orderDate))},${order.totalAmount},${order.status}")
                }
            }
            "purchase" -> {
                appendLine("OrderNo,Date,Amount,Status")
                val filtered = purchaseOrders.filter { it.orderDate in startDate..endDate }
                filtered.forEach { order ->
                    appendLine("${order.orderNo},${dateFormat.format(Date(order.orderDate))},${order.totalAmount},${order.status}")
                }
            }
            "finance" -> {
                appendLine("Date,Account,Type,Amount,Category")
                val filtered = transactions.filter { it.transactionDate in startDate..endDate }
                filtered.forEach { tx ->
                    val typeLabel = if (tx.type == "income") "Income" else "Expense"
                    appendLine("${dateFormat.format(Date(tx.transactionDate))},${tx.accountName},$typeLabel,${tx.amount},${tx.category}")
                }
            }
            "inventory" -> {
                appendLine("Product,Warehouse,Quantity,MinStock")
                inventory.forEach { item ->
                    appendLine("${item.productName},${item.warehouse},${item.quantity},${item.minStock}")
                }
            }
        }
    }
    
    file.writeText(content, Charsets.UTF_8)
    file
}
