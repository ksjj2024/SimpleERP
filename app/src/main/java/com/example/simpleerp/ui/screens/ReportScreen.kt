package com.example.simpleerp.ui.screens

import android.content.Context
import android.content.Intent
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
import java.io.FileOutputStream
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
        "sales" to "閿€鍞姤琛?,
        "purchase" to "閲囪喘鎶ヨ〃",
        "finance" to "璐㈠姟鎶ヨ〃",
        "inventory" to "搴撳瓨鎶ヨ〃"
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
            Text("鎶ヨ〃缁熻", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("鏃ユ湡鑼冨洿", style = MaterialTheme.typography.titleMedium)
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
                        ) {
                            Text("杩?澶?)
                        }
                        OutlinedButton(
                            onClick = {
                                startDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                                endDate = System.currentTimeMillis()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("杩?0澶?)
                        }
                        OutlinedButton(
                            onClick = {
                                startDate = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
                                endDate = System.currentTimeMillis()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("鏈勾")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("浠? ${dateFormat.format(Date(startDate))} 鍒? ${dateFormat.format(Date(endDate))}")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("鎶ヨ〃绫诲瀷", style = MaterialTheme.typography.titleMedium)
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
                        reportTypes.find { it.first == selectedReportType }?.second ?: "鎶ヨ〃",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedReportType) {
                        "sales" -> {
                            StatRow("閿€鍞鍗曟暟", "${filteredSales.size}")
                            StatRow("閿€鍞€婚", "楼${String.format("%.2f", totalSales)}")
                            StatRow("骞冲潎璁㈠崟閲戦", "楼${if (filteredSales.isNotEmpty()) String.format("%.2f", totalSales / filteredSales.size) else "0.00"}")
                        }
                        "purchase" -> {
                            StatRow("閲囪喘璁㈠崟鏁?, "${filteredPurchase.size}")
                            StatRow("閲囪喘鎬婚", "楼${String.format("%.2f", totalPurchase)}")
                            StatRow("骞冲潎璁㈠崟閲戦", "楼${if (filteredPurchase.isNotEmpty()) String.format("%.2f", totalPurchase / filteredPurchase.size) else "0.00"}")
                        }
                        "finance" -> {
                            StatRow("鎬绘敹鍏?, "楼${String.format("%.2f", totalIncome)}")
                            StatRow("鎬绘敮鍑?, "楼${String.format("%.2f", totalExpense)}")
                            StatRow("鍑€鍒╂鼎", "楼${String.format("%.2f", profit)}")
                        }
                        "inventory" -> {
                            StatRow("搴撳瓨鍟嗗搧鏁?, "${inventory.size}")
                            StatRow("搴撳瓨鎬婚噺", "${inventory.sumOf { it.quantity }}")
                            val lowStock = inventory.filter { it.quantity <= it.minStock }.size
                            StatRow("浣庡簱瀛樺晢鍝佹暟", "$lowStock")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("瀵煎嚭鎶ヨ〃", style = MaterialTheme.typography.titleMedium)
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
                                    exportMessage = "宸插鍑? ${file.name}"
                                } catch (e: Exception) {
                                    exportMessage = "瀵煎嚭澶辫触: ${e.message}"
                                }
                                isExporting = false
                            }
                        },
                        enabled = !isExporting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("瀵煎嚭CSV鎶ヨ〃")
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
                appendLine("璁㈠崟鍙?鏃ユ湡,閲戦,鐘舵€?)
                val filtered = salesOrders.filter { it.orderDate in startDate..endDate }
                filtered.forEach { order ->
                    appendLine("${order.orderNo},${dateFormat.format(Date(order.orderDate))},${order.totalAmount},${order.status}")
                }
                appendLine()
                appendLine("鎬昏,,${filtered.sumOf { it.totalAmount }},")
            }
            "purchase" -> {
                appendLine("璁㈠崟鍙?鏃ユ湡,閲戦,鐘舵€?)
                val filtered = purchaseOrders.filter { it.orderDate in startDate..endDate }
                filtered.forEach { order ->
                    appendLine("${order.orderNo},${dateFormat.format(Date(order.orderDate))},${order.totalAmount},${order.status}")
                }
                appendLine()
                appendLine("鎬昏,,${filtered.sumOf { it.totalAmount }},")
            }
            "finance" -> {
                appendLine("鏃ユ湡,璐︽埛,绫诲瀷,閲戦,鍒嗙被")
                val filtered = transactions.filter { it.transactionDate in startDate..endDate }
                filtered.forEach { tx ->
                    appendLine("${dateFormat.format(Date(tx.transactionDate))},${tx.accountName},${if(tx.type=="income")"鏀跺叆" else "鏀嚭"},${tx.amount},${tx.category}")
                }
                val income = filtered.filter { it.type == "income" }.sumOf { it.amount }
                val expense = filtered.filter { it.type == "expense" }.sumOf { it.amount }
                appendLine()
                appendLine("鎬绘敹鍏?,,${income},")
                appendLine("鎬绘敮鍑?,,${expense},")
                appendLine("鍑€鍒╂鼎,,,${income - expense},")
            }
            "inventory" -> {
                appendLine("鍟嗗搧鍚嶇О,浠撳簱,搴撳瓨鏁伴噺,鏈€浣庡簱瀛?)
                inventory.forEach { item ->
                    appendLine("${item.productName},${item.warehouse},${item.quantity},${item.minStock}")
                }
            }
        }
    }
    
    file.writeText(content, Charsets.UTF_8)
    file
}
