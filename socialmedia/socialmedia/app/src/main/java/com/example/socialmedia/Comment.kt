package com.example.socialmedia

import java.io.Serializable

data class Comment(
    val content: String = "",
    val authorId: String? = null,
    val authorDisplayName: String? = null
) : Serializable
