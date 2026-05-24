package com.example.vehiclecost.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vehiclecost.data.dao.CostDao
import com.example.vehiclecost.data.entity.VehicleCost

@Database(entities = [VehicleCost::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun costDao(): CostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vehicle_cost_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
