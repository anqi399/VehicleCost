package com.example.vehiclecost.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vehiclecost.data.dao.CostDao
import com.example.vehiclecost.data.entity.VehicleCost

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [VehicleCost::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun costDao(): CostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE costs ADD COLUMN tag TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vehicle_cost_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("UPDATE costs SET category = '过路费' WHERE category = '充电'")
                        db.execSQL("UPDATE costs SET category = '维修保养' WHERE category = '保养'")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
