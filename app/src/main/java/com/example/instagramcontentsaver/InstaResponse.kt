package com.example.instagramcontentsaver

data class InstaResponse(var graphql:instaObject)

data class instaObject(var shortcode_media:contentObject)

data class contentObject(
    var video_url:String="",
    var display_url:String=""
)