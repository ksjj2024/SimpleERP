package com.example.simpleerp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNo: String,
    val supplierId: Long,
    val orderDate: Long,
    val totalAmount: Double = 0.0,
    val status: String = "pending", // pending, received, cancelled
    val remark: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_order_items")
data class PurchaseOrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val amount: Double
)
