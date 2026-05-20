package com.example.compoundingjournal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.compoundingjournal.data.dao.TradeDao
import com.example.compoundingjournal.data.entity.TradeEntity

@Database(entities = [TradeEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
}
