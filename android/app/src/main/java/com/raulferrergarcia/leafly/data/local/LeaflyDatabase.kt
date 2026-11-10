package com.raulferrergarcia.leafly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlantEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class LeaflyDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
}
