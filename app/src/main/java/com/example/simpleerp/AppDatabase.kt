package com.example.simpleerp

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.simpleerp.dao.*
import com.example.simpleerp.entity.*

@Database(
    entities = [
        Product::class,
        Customer::class,
        Supplier::class,
        PurchaseOrder::class,
        PurchaseOrderItem::class,
        SalesOrder::class,
        SalesOrderItem::class,
        Inventory::class,
        Account::class,
        Transaction::class,
        ProductionOrder::class,
        ProductionItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
    abstract fun salesOrderDao(): SalesOrderDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun productionOrderDao(): ProductionOrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simple_erp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
