package com.example.flo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Song::class, Album::class, User::class, Like::class], version = 3)
abstract class SongDatabase : RoomDatabase(){
    abstract fun albumDao() : AlbumDao
    abstract fun songDao() : SongDao
    abstract fun userDao() : UserDao

    companion object {
        private var instance: SongDatabase? = null

        @Synchronized
        fun getInstance(context: Context) : SongDatabase? {
            if (instance == null) {
                synchronized(SongDatabase::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SongDatabase::class.java,
                        "song-database"
                    ).allowMainThreadQueries().build()
                }
            }
            return instance
        }



    }
}