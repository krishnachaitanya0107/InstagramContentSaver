package com.example.instagramcontentsaver.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [InstaContent::class], version = 1, exportSchema = false)

@TypeConverters(Converters::class)
abstract class DataBase : RoomDatabase() {

    abstract fun instaContentDao(): InstaContentDao

    companion object {

        @Volatile
        private var instance: DataBase? = null

        @Synchronized
        fun getInstance(context: Context): DataBase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBase::class.java,
                    "content.db"
                ).fallbackToDestructiveMigration()
                    .build()
            return instance!!

        }
    }

}