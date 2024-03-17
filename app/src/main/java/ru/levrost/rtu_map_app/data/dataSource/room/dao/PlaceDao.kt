package ru.levrost.rtu_map_app.data.dataSource.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.levrost.rtu_map_app.data.dataSource.room.entites.PlaceEntity

@Dao
interface PlaceDao {

    @Query("SELECT * FROM PlaceEntity")
    fun getAllPlaces(): LiveData<List<PlaceEntity>>?

    @Query("SELECT * FROM PlaceEntity WHERE userName LIKE '%' || :text || '%' OR description LIKE '%' || :text || '%' OR name LIKE '%' || :text || '%'")
    fun getPlacesByText(text: String): LiveData<List<PlaceEntity>>?

    @Query("SELECT COUNT(*) FROM PlaceEntity")
    fun count(): LiveData<Int>?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addPlace(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun pushPlace(place: List<PlaceEntity>)

    @Delete
    fun deletePlace(place: PlaceEntity)

    @Query("DELETE FROM PlaceEntity")
    fun clearData()
}