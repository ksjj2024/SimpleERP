package com.example.simpleerp.dao

import androidx.room.*
import com.example.simpleerp.entity.ProductionOrder
import com.example.simpleerp.entity.ProductionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionOrderDao {
    @Query("SELECT * FROM production_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<ProductionOrder>>

    @Query("SELECT * FROM production_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): ProductionOrder?

    @Query("SELECT * FROM production_items WHERE productionOrderId = :orderId")
    suspend fun getProductionItems(orderId: Long): List<ProductionItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: ProductionOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ProductionItem): Long

    @Update
    suspend fun updateOrder(order: ProductionOrder)

    @Query("UPDATE production_orders SET status = :status, completedQuantity = :completedQuantity WHERE id = :id")
    suspend fun updateStatusAndQuantity(id: Long, status: String, completedQuantity: Double)

    @Delete
    suspend fun deleteOrder(order: ProductionOrder)

    @Query("DELETE FROM production_items WHERE productionOrderId = :orderId")
    suspend fun deleteItemsByOrderId(orderId: Long)
}
