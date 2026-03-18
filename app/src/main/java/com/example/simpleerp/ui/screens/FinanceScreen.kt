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
fun FinanceScreen() {
    val db = SimpleERPApplication().database
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        accounts = db.accountDao().getAllAccounts().first()
        transactions = db.transactionDao().getAllTransactions().first()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddAccountDialog = true
                    else showAddTransactionDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("账户") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("流水") }
                )
            }

            if (selectedTab == 0) {
                // Accounts Tab
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts) { account ->
                        AccountItem(account)
                    }
                }
            } else {
                // Transactions Tab
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { account ->
                accounts = accounts + account
                showAddAccountDialog = false
            }
        )
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            accounts = accounts,
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { transaction ->
                transactions = transactions + transaction
                showAddTransactionDialog = false
            }
        )
    }
}

@Composable
fun AccountItem(account: Account) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.titleMedium)
                Text("类型: ${getAccountTypeName(account.type)}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "¥${String.format("%.2f", account.balance)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val isIncome = transaction.type == "income"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description.ifEmpty { transaction.category }, style = MaterialTheme.typography.titleMedium)
                Text("账户: ${transaction.accountName}", style = MaterialTheme.typography.bodySmall)
                Text("日期: ${dateFormat.format(Date(transaction.transactionDate))}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "${if (isIncome) "+" else "-"}¥${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

fun getAccountTypeName(type: String): String = when (type) {
    "cash" -> "现金"
    "bank" -> "银行"
    "account_receivable" -> "应收账款"
    "account_payable" -> "应付账款"
    else -> type
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(onDismiss: () -> Unit, onConfirm: (Account) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("cash") }
    var balance by remember { mutableStateOf("0") }
    var typeExpanded by remember { mutableStateOf(false) }

    val accountTypes = listOf("cash" to "现金", "bank" to "银行", "account_receivable" to "应收账款", "account_payable" to "应付账款")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加账户") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = accountTypes.find { it.first == type }?.second ?: "现金",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("账户类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        accountTypes.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    type = value
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("初始余额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val account = Account(
                        name = name,
                        type = type,
                        balance = balance.toDoubleOrNull() ?: 0.0
                    )
                    onConfirm(account)
                },
                enabled = name.isNotEmpty()
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var type by remember { mutableStateOf("income") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var accountExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    val transactionTypes = listOf("income" to "收入", "expense" to "支出")
    val categories = if (type == "income") listOf("销售收款", "其他收入") else listOf("采购付款", "工资", "租金", "其他支出")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建流水") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == selectedAccountId }?.name ?: "选择账户",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("账户") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = transactionTypes.find { it.first == type }?.second ?: "收入",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        transactionTypes.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    type = value
                                    category = ""
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category.ifEmpty { "选择分类" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分类") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val account = accounts.find { it.id == selectedAccountId }
                    val transaction = Transaction(
                        accountId = selectedAccountId ?: 0,
                        accountName = account?.name ?: "",
                        type = type,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        category = category,
                        description = description
                    )
                    onConfirm(transaction)
                },
                enabled = selectedAccountId != null && amount.toDoubleOrNull() != null && category.isNotEmpty()
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
}
