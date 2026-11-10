package com.raulferrergarcia.leafly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PlantEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class LeaflyDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao

    companion object {
        /**
         * Añade la fecha del último riego sin perder las plantas existentes.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plants ADD COLUMN lastWateredAt INTEGER")
            }
        }

        /**
         * Añade la foto opcional de la planta sin perder las plantas existentes.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plants ADD COLUMN photoData BLOB")
            }
        }
    }
}
