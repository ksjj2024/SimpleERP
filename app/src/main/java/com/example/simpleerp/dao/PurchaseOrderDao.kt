package com.example.simpleerp.dao

import androidx.room.*
import com.example.simpleerp.entity.PurchaseOrder
import com.example.simpleerp.entity.PurchaseOrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseOrderDao {
    @Query("SELECT * FROM purchase_orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<PurchaseOrder>>

    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): PurchaseOrder?

    @Query("SELECT * FROM purchase_order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Long): List<PurchaseOrderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: PurchaseOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PurchaseOrderItem): Long

    @Update
    suspend fun updateOrder(order: PurchaseOrder)

    @Delete
    suspend fun deleteOrder(order: PurchaseOrder)

    @Query("DELETE FROM purchase_order_items WHERE orderId = :orderId")
    suspend fun deleteItemsByOrderId(orderId: Long)

    @Query("UPDATE purchase_orders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
