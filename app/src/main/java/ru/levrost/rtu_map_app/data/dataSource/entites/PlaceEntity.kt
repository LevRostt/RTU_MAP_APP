package ru.levrost.rtu_map_app.data.dataSource.entites

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
data class PlaceEntity(
    val name: String,
    val idPlace: String,
    val description: String,
    val icon: String,
    val userName: String,
    val userId: String,
    val likes: Int,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
{
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}