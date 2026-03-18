package com.example.simpleerp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contact: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val remark: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
