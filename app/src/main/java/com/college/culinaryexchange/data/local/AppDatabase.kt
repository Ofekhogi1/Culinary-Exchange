package com.college.culinaryexchange.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.college.culinaryexchange.data.local.dao.PostDao
import com.college.culinaryexchange.data.local.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "culinary_exchange.db"
                ).build().also { INSTANCE = it }
            }
    }
}
