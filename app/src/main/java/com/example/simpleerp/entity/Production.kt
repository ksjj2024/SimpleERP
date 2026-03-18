package com.example.simpleerp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_orders")
data class ProductionOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNo: String,
    val productId: Long,
    val productName: String,
    val plannedQuantity: Double,
    val completedQuantity: Double = 0.0,
    val status: String = "planned", // planned, in_progress, completed, cancelled
    val startDate: Long,
    val endDate: Long? = null,
    val remark: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "production_items")
data class ProductionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productionOrderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unit: String = "件"
)
