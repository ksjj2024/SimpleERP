package com.example.simpleerp.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.simpleerp.SimpleERPApplication
import com.example.simpleerp.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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
        "sales" to "销售报表",
        "purchase" to "采购报表",
        "finance" to "财务报表",
        "inventory" to "库存报表"
    )

    LaunchedEffect(Unit) {
        salesOrders = db.salesOrderDao().getAllOrders().first()
        purchaseOrders = db.purchaseOrderDao().getAllOrders().first()
        transactions = db.transactionDao().getAllTransactions().first()
        inventory = db.inventoryDao().getAllInventory().first()
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Calculate statistics
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
            Text("报表统计", style = MaterialTheme.typography.headlineMedium)
        }

        // Date Range Selection
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("日期范围", style = MaterialTheme.typography.titleMedium)
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
                            Text("近7天")
                        }
                        OutlinedButton(
                            onClick = {
                                startDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                                endDate = System.currentTimeMillis()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("近30天")
                        }
                        OutlinedButton(
                            onClick = {
                                startDate = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
                                endDate = System.currentTimeMillis()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("本年")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("从: ${dateFormat.format(Date(startDate))} 到: ${dateFormat.format(Date(endDate))}")
                }
            }
        }

        // Report Type Selection
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("报表类型", style = MaterialTheme.typography.titleMedium)
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

        // Statistics based on selected report type
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        reportTypes.find { it.first == selectedReportType }?.second ?: "报表",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedReportType) {
                        "sales" -> {
                            StatRow("销售订单数", "${filteredSales.size}")
                            StatRow("销售总额", "¥${String.format("%.2f", totalSales)}")
                            StatRow("平均订单金额", "¥${if (filteredSales.isNotEmpty()) String.format("%.2f", totalSales / filteredSales.size) else "0.00"}")
                        }
                        "purchase" -> {
                            StatRow("采购订单数", "${filteredPurchase.size}")
                            StatRow("采购总额", "¥${String.format("%.2f", totalPurchase)}")
                            StatRow("平均订单金额", "¥${if (filteredPurchase.isNotEmpty()) String.format("%.2f", totalPurchase / filteredPurchase.size) else "0.00"}")
                        }
                        "finance" -> {
                            StatRow("总收入", "¥${String.format("%.2f", totalIncome)}")
                            StatRow("总支出", "¥${String.format("%.2f", totalExpense)}")
                            StatRow("净利润", "¥${String.format("%.2f", profit)}")
                        }
                        "inventory" -> {
                            val totalValue = inventory.sumOf { it.quantity * (it.quantity * 10) } // Simplified
                            StatRow("库存商品数", "${inventory.size}")
                            StatRow("库存总量", "${inventory.sumOf { it.quantity }}")
                            val lowStock = inventory.filter { it.quantity <= it.minStock }.size
                            StatRow("低库存商品数", "$lowStock")
                        }
                    }
                }
            }
        }

        // Export Buttons
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("导出报表", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (exportMessage.isNotEmpty()) {
                        Text(exportMessage, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isExporting = true
                                    try {
                                        val file = exportToExcel(
                                            context,
                                            selectedReportType,
                                            startDate,
                                            endDate,
                                            salesOrders,
                                            purchaseOrders,
                                            transactions,
                                            inventory
                                        )
                                        exportMessage = "已导出: ${file.name}"
                                    } catch (e: Exception) {
                                        exportMessage = "导出失败: ${e.message}"
                                    }
                                    isExporting = false
                                }
                            },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileExcel, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出Excel")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isExporting = true
                                    try {
                                        val file = exportToPdf(
                                            context,
                                            selectedReportType,
                                            startDate,
                                            endDate,
                                            salesOrders,
                                            purchaseOrders,
                                            transactions,
                                            inventory
                                        )
                                        exportMessage = "已导出: ${file.name}"
                                    } catch (e: Exception) {
                                        exportMessage = "导出失败: ${e.message}"
                                    }
                                    isExporting = false
                                }
                            },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出PDF")
                        }
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

suspend fun exportToExcel(
    context: Context,
    reportType: String,
    startDate: Long,
    endDate: Long,
    salesOrders: List<SalesOrder>,
    purchaseOrders: List<PurchaseOrder>,
    transactions: List<Transaction>,
    inventory: List<Inventory>
): File = withContext(Dispatchers.IO) {
    val workbook = XSSFWorkbook()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    when (reportType) {
        "sales" -> {
            val sheet = workbook.createSheet("销售报表")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("订单号")
            header.createCell(1).setCellValue("日期")
            header.createCell(2).setCellValue("金额")
            header.createCell(3).setCellValue("状态")

            val filtered = salesOrders.filter { it.orderDate in startDate..endDate }
            filtered.forEachIndexed { index, order ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(order.orderNo)
                row.createCell(1).setCellValue(dateFormat.format(Date(order.orderDate)))
                row.createCell(2).setCellValue(order.totalAmount)
                row.createCell(3).setCellValue(order.status)
            }
        }
        "purchase" -> {
            val sheet = workbook.createSheet("采购报表")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("订单号")
            header.createCell(1).setCellValue("日期")
            header.createCell(2).setCellValue("金额")
            header.createCell(3).setCellValue("状态")

            val filtered = purchaseOrders.filter { it.orderDate in startDate..endDate }
            filtered.forEachIndexed { index, order ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(order.orderNo)
                row.createCell(1).setCellValue(dateFormat.format(Date(order.orderDate)))
                row.createCell(2).setCellValue(order.totalAmount)
                row.createCell(3).setCellValue(order.status)
            }
        }
        "finance" -> {
            val sheet = workbook.createSheet("财务报表")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("日期")
            header.createCell(1).setCellValue("账户")
            header.createCell(2).setCellValue("类型")
            header.createCell(3).setCellValue("金额")
            header.createCell(4).setCellValue("分类")

            val filtered = transactions.filter { it.transactionDate in startDate..endDate }
            filtered.forEachIndexed { index, tx ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(dateFormat.format(Date(tx.transactionDate)))
                row.createCell(1).setCellValue(tx.accountName)
                row.createCell(2).setCellValue(if (tx.type == "income") "收入" else "支出")
                row.createCell(3).setCellValue(tx.amount)
                row.createCell(4).setCellValue(tx.category)
            }
        }
        "inventory" -> {
            val sheet = workbook.createSheet("库存报表")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("商品名称")
            header.createCell(1).setCellValue("仓库")
            header.createCell(2).setCellValue("库存数量")
            header.createCell(3).setCellValue("最低库存")

            inventory.forEachIndexed { index, item ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(item.productName)
                row.createCell(1).setCellValue(item.warehouse)
                row.createCell(2).setCellValue(item.quantity)
                row.createCell(3).setCellValue(item.minStock)
            }
        }
    }

    val fileName = "ERP_${reportType}_${System.currentTimeMillis()}.xlsx"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
    FileOutputStream(file).use { workbook.write(it) }
    workbook.close()
    file
}

suspend fun exportToPdf(
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
    
    val fileName = "ERP_${reportType}_${System.currentTimeMillis()}.pdf"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
    
    // Simple text-based PDF export (in production, use iText7)
    val content = buildString {
        appendLine("=".repeat(50))
        appendLine("ERP ${getReportTitle(reportType)}")
        appendLine("日期范围: ${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}")
        appendLine("=".repeat(50))
        appendLine()
        
        when (reportType) {
            "sales" -> {
                val filtered = salesOrders.filter { it.orderDate in startDate..endDate && it.status != "cancelled" }
                appendLine("销售订单数: ${filtered.size}")
                appendLine("销售总额: ¥${String.format("%.2f", filtered.sumOf { it.totalAmount })}")
                appendLine()
                appendLine("订单明细:")
                filtered.forEach { order ->
                    appendLine("  ${order.orderNo} | ${dateFormat.format(Date(order.orderDate))} | ¥${order.totalAmount} | ${order.status}")
                }
            }
            "purchase" -> {
                val filtered = purchaseOrders.filter { it.orderDate in startDate..endDate && it.status != "cancelled" }
                appendLine("采购订单数: ${filtered.size}")
                appendLine("采购总额: ¥${String.format("%.2f", filtered.sumOf { it.totalAmount })}")
                appendLine()
                appendLine("订单明细:")
                filtered.forEach { order ->
                    appendLine("  ${order.orderNo} | ${dateFormat.format(Date(order.orderDate))} | ¥${order.totalAmount} | ${order.status}")
                }
            }
            "finance" -> {
                val filtered = transactions.filter { it.transactionDate in startDate..endDate }
                val income = filtered.filter { it.type == "income" }.sumOf { it.amount }
                val expense = filtered.filter { it.type == "expense" }.sumOf { it.amount }
                appendLine("总收入: ¥${String.format("%.2f", income)}")
                appendLine("总支出: ¥${String.format("%.2f", expense)}")
                appendLine("净利润: ¥${String.format("%.2f", income - expense)}")
                appendLine()
                appendLine("交易明细:")
                filtered.forEach { tx ->
                    val typeLabel = if (tx.type == "income") "收入" else "支出"
                    appendLine("  ${dateFormat.format(Date(tx.transactionDate))} | ${tx.accountName} | $typeLabel | ¥${tx.amount} | ${tx.category}")
                }
            }
            "inventory" -> {
                appendLine("库存商品数: ${inventory.size}")
                appendLine("库存总量: ${inventory.sumOf { it.quantity }}")
                val lowStock = inventory.filter { it.quantity <= it.minStock }.size
                appendLine("低库存商品数: $lowStock")
                appendLine()
                appendLine("库存明细:")
                inventory.forEach { item ->
                    appendLine("  ${item.productName} | ${item.warehouse} | ${item.quantity} | 最低: ${item.minStock}")
                }
            }
        }
    }
    
    // Write as text file (for actual PDF, integrate iText7 properly)
    file.writeText(content)
    file
}

fun getReportTitle(type: String): String = when (type) {
    "sales" -> "销售报表"
    "purchase" -> "采购报表"
    "finance" -> "财务报表"
    "inventory" -> "库存报表"
    else -> "报表"
}
