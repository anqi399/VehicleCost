package com.example.vehiclecost.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "costs")
data class VehicleCost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String, // 加油、充电、洗车、保养、保险、其他
    val date: Long, // timestamp
    val monthStr: String, // YYYY-MM 格式，用于快速按月聚合查询
    val note: String,
    val tag: String = ""
)
