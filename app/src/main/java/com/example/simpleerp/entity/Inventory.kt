package com.example.simpleerp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class Inventory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val warehouse: String = "默认仓库",
    val minStock: Double = 0.0,
    val maxStock: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)
