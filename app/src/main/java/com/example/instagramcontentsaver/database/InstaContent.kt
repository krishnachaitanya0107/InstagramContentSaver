package com.example.instagramcontentsaver.database


import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instaContent")
data class InstaContent(
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    var contentUri: Uri,
    var contentLink:String?="",
    var fileName:String?="",
    var savedAt: Long =System.currentTimeMillis(),
)