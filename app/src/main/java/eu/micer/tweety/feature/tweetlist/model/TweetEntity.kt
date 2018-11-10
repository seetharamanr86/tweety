package eu.micer.tweety.feature.tweetlist.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity
data class TweetEntity(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id: Int? = null,
    val text: String,
    val user: String,
    val timestamp: String
)