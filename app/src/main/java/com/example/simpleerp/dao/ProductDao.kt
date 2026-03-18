package com.example.simpleerp.dao

import androidx.room.*
import com.example.simpleerp.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE code = :code")
    suspend fun getProductByCode(code: String): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :keyword || '%' OR code LIKE '%' || :keyword || '%'")
    fun searchProducts(keyword: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)
}
