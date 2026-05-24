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

    @Insert
    fun insertCosts(costs: List<VehicleCost>)

    @Delete
    fun deleteCost(cost: VehicleCost)

    @Update
    fun updateCost(cost: VehicleCost)

    @Query("SELECT * FROM costs ORDER BY date DESC")
    fun getAllCosts(): Flow<List<VehicleCost>>

    @Query("SELECT * FROM costs")
    fun getAllCostsSnapshot(): List<VehicleCost>

    @Query("SELECT * FROM costs WHERE monthStr LIKE :periodPattern ORDER BY date DESC")
    fun getCostsByPeriod(periodPattern: String): Flow<List<VehicleCost>>

    @Query("SELECT * FROM costs WHERE monthStr LIKE :periodPattern AND category = :category ORDER BY date DESC")
    fun getCostsByPeriodAndCategory(periodPattern: String, category: String): Flow<List<VehicleCost>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM costs WHERE monthStr LIKE :periodPattern")
    fun getTotalAmountByPeriod(periodPattern: String): Flow<Double>

    @Query("SELECT * FROM costs WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getCostsBetween(startDate: Long, endDate: Long): Flow<List<VehicleCost>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM costs WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalAmountBetween(startDate: Long, endDate: Long): Flow<Double>
}
