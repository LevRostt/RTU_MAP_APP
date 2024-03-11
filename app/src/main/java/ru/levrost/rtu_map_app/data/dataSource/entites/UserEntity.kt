package ru.levrost.rtu_map_app.data.dataSource.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
data class UserEntity(
    @PrimaryKey
    val id: Long = 0,
    val name: String,
    val userId: String,

    @TypeConverters(ListConvertor::class)
    val likes: List<String>,

    @TypeConverters(ListConvertor::class)
    val subscribes: List<String>,

    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)