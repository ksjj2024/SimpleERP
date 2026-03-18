package com.example.simpleerp.dao

import androidx.room.*
import com.example.simpleerp.entity.SalesOrder
import com.example.simpleerp.entity.SalesOrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesOrderDao {
    @Query("SELECT * FROM sales_orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<SalesOrder>>

    @Query("SELECT * FROM sales_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): SalesOrder?

    @Query("SELECT * FROM sales_order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Long): List<SalesOrderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: SalesOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SalesOrderItem): Long

    @Update
    suspend fun updateOrder(order: SalesOrder)

    @Delete
    suspend fun deleteOrder(order: SalesOrder)

    @Query("DELETE FROM sales_order_items WHERE orderId = :orderId")
    suspend fun deleteItemsByOrderId(orderId: Long)

    @Query("UPDATE sales_orders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE sales_orders SET paidAmount = paidAmount + :amount WHERE id = :id")
    suspend fun updatePaidAmount(id: Long, amount: Double)
}
