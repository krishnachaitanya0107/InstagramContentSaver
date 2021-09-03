package com.example.instagramcontentsaver.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface InstaContentDao {

    @Query("SELECT* FROM instaContent ORDER BY savedAt DESC")
    fun getAllContent():LiveData<List<InstaContent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addContent(post:InstaContent)

    @Query("DELETE FROM instaContent WHERE id=:id")
    fun deleteContentById(id:String)

    @Query("DELETE FROM instaContent")
    fun deleteAllContent()

}