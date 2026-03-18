package com.example.simpleerp

import android.app.Application

class SimpleERPApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
