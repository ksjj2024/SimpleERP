package com.example.simpleerp.dao

import androidx.room.*
import com.example.simpleerp.entity.Inventory
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY productName ASC")
    fun getAllInventory(): Flow<List<Inventory>>

    @Query("SELECT * FROM inventory WHERE productId = :productId")
    suspend fun getInventoryByProductId(productId: Long): Inventory?

    @Query("SELECT * FROM inventory WHERE productName LIKE '%' || :keyword || '%'")
    fun searchInventory(keyword: String): Flow<List<Inventory>>

    @Query("SELECT * FROM inventory WHERE quantity <= minStock")
    fun getLowStockItems(): Flow<List<Inventory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventory: Inventory): Long

    @Update
    suspend fun update(inventory: Inventory)

    @Query("UPDATE inventory SET quantity = quantity + :amount WHERE productId = :productId")
    suspend fun increaseQuantity(productId: Long, amount: Double)

    @Query("UPDATE inventory SET quantity = quantity - :amount WHERE productId = :productId")
    suspend fun decreaseQuantity(productId: Long, amount: Double)

    @Delete
    suspend fun delete(inventory: Inventory)
}
