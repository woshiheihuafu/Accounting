package com.cocos.androidaccounting.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cocos.androidaccounting.data.local.converter.RoomConverters

@Database(entities = [BillEntity::class], version = 1, exportSchema = true)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao

    companion object {
        const val DATABASE_NAME = "accounting.db"
    }
}
