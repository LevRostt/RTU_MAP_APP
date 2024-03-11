package ru.levrost.rtu_map_app.data.dataSource.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.levrost.rtu_map_app.data.dataSource.entites.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM UserEntity")
    fun getData(): LiveData<UserEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun editProfile(profile: UserEntity)
}