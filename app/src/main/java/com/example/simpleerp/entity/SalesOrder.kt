package com.example.simpleerp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_orders")
data class SalesOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNo: String,
    val customerId: Long,
    val orderDate: Long,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "pending", // pending, completed, cancelled
    val remark: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales_order_items")
data class SalesOrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val amount: Double
)
