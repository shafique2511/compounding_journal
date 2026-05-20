package com.example.compoundingjournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.compoundingjournal.data.dao.SettingsDao
import com.example.compoundingjournal.data.dao.TradeDao
import com.example.compoundingjournal.data.entity.SettingsEntity
import com.example.compoundingjournal.data.entity.TradeEntity

@Database(entities = [TradeEntity::class, SettingsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeDao(): TradeDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "compounding_journal_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
