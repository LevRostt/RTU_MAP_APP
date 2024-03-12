package ru.levrost.rtu_map_app.data.dataSource.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.levrost.rtu_map_app.data.dataSource.entites.PlaceEntity

@Dao
interface PlaceDao {

    @Query("SELECT * FROM PlaceEntity")
    fun getAllPlaces(): LiveData<List<PlaceEntity>>?

    @Query("SELECT COUNT(*) FROM PlaceEntity")
    fun count(): LiveData<Int>?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addPlace(place: PlaceEntity)

    @Delete
    fun deletePlace(place: PlaceEntity)
}