package ru.levrost.rtu_map_app.data.dataSource.root

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteOpenHelper
import ru.levrost.rtu_map_app.data.dataSource.dao.UserDao
import ru.levrost.rtu_map_app.data.dataSource.entites.ListConvertor
import ru.levrost.rtu_map_app.data.dataSource.entites.UserEntity
import java.util.concurrent.Executors
import kotlin.concurrent.Volatile

@Database(entities = [UserEntity::class], version = 1)
@TypeConverters(ListConvertor::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun userDao() : UserDao?

    companion object{
        private val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        @Volatile
        private var dbInstance: AppDataBase? = null

        fun getDataBase(context: Context): AppDataBase {
            if (dbInstance == null) {
                synchronized(AppDataBase::class) {
                    if (dbInstance == null) {
                        dbInstance = builderDB(context)
                    }
                }
            }
            return dbInstance!!
        }
        private fun builderDB(context: Context) : AppDataBase{
            return databaseBuilder(context.applicationContext, AppDataBase::class.java, "rtu_map_app_database")
                .fallbackToDestructiveMigration() // Очищает базу данных при не совпадении версий(Заменить на миграцию, если будет необходимость)
                .build()
        }
    }

    override fun clearAllTables() {
        TODO("Not yet implemented")
    }

    override fun createInvalidationTracker(): InvalidationTracker {
        TODO("Not yet implemented")
    }

    override fun createOpenHelper(config: DatabaseConfiguration): SupportSQLiteOpenHelper {
        TODO("Not yet implemented")
    }

}