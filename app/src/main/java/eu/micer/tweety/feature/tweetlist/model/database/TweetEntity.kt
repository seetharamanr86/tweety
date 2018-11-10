package eu.micer.tweety.feature.tweetlist.model.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class TweetEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var tweetId: Long,
    var text: String,
    var user: String,
    var createdAt: String,  // TODO convert to Date
    var timestamp: String
)