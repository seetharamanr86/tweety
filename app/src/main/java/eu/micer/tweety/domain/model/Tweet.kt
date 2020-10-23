package eu.micer.tweety.domain.model

import java.util.*

data class Tweet(
    val id: Int = 0,
    val tweetId: Long,
    val text: String,
    val user: String,
    val createdAt: Date?,
    val timestamp: Long = System.currentTimeMillis()
)
