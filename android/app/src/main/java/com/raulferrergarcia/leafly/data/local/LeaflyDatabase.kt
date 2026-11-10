package com.raulferrergarcia.leafly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PlantEntity::class, PlantPhotoEntity::class],
    version = 4,
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

        /**
         * Crea la tabla de historial de fotos de las plantas.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS plant_photos (
                        id TEXT NOT NULL,
                        plantId TEXT NOT NULL,
                        addedAt INTEGER NOT NULL,
                        photoData BLOB NOT NULL,
                        PRIMARY KEY (id),
                        FOREIGN KEY (plantId) REFERENCES plants (id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_plant_photos_plantId ON plant_photos (plantId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_plant_photos_addedAt ON plant_photos (addedAt)")
            }
        }
    }
}
