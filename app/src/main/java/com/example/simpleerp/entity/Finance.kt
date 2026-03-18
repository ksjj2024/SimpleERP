package com.example.simpleerp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // cash, bank, account_receivable, account_payable
    val balance: Double = 0.0,
    val remark: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val accountName: String,
    val type: String, // income, expense, transfer
    val amount: Double,
    val category: String,
    val description: String = "",
    val relatedId: Long? = null, // related order id
    val relatedType: String? = null, // purchase, sales
    val transactionDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
