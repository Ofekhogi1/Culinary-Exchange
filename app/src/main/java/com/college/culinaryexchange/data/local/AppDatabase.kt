package com.college.culinaryexchange.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.college.culinaryexchange.data.local.converter.Converters
import com.college.culinaryexchange.data.local.dao.PostDao
import com.college.culinaryexchange.data.local.entity.PostEntity

@Database(entities = [PostEntity::class], version = DB_VERSION, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                // TODO: replace destructive migration with a proper Migration object
                // before shipping to production.
                .fallbackToDestructiveMigration(true)
                .build()
                .also { INSTANCE = it }
            }
    }
}

private const val DB_VERSION = 2
private const val DB_NAME = "culinary_exchange.db"
