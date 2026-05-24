package com.example.vehiclecost.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.vehiclecost.data.entity.VehicleCost
import kotlinx.coroutines.flow.Flow

@Dao
interface CostDao {
    @Insert
    fun insertCost(cost: VehicleCost)

    @Delete
    fun deleteCost(cost: VehicleCost)

    @Update
    fun updateCost(cost: VehicleCost)

    @Query("SELECT * FROM costs ORDER BY date DESC")
    fun getAllCosts(): Flow<List<VehicleCost>>

    @Query("SELECT * FROM costs WHERE monthStr LIKE :periodPattern ORDER BY date DESC")
    fun getCostsByPeriod(periodPattern: String): Flow<List<VehicleCost>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM costs WHERE monthStr LIKE :periodPattern")
    fun getTotalAmountByPeriod(periodPattern: String): Flow<Double>
}
